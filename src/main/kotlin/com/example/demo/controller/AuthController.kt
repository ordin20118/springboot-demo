package com.example.demo.controller

import com.example.demo.domain.TokenPlatform
import com.example.demo.dto.*
import com.example.demo.service.AccessTokenService
import com.example.demo.service.UserService
import com.example.demo.service.AppleTokenValidationService
import com.example.demo.service.AppleTokenRevokeService
import com.example.demo.service.KakaoTokenValidationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.MessageDigest
import java.util.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "사용자 인증 관련 API")
class AuthController(
    private val userService: UserService,
    private val accessTokenService: AccessTokenService,
    private val appleTokenValidationService: AppleTokenValidationService,
    private val appleTokenRevokeService: AppleTokenRevokeService,
    private val kakaoTokenValidationService: KakaoTokenValidationService
) {
    
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    fun signup(@Valid @RequestBody request: UserSignupRequest): ResponseEntity<UserResponse> {
        val userResponse = userService.signup(request)
        return ResponseEntity.ok(userResponse)
    }
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인 후 토큰 해시를 반환합니다")
    fun login(
        @Valid @RequestBody request: UserLoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<TokenResponse> {
        val user = when {
            // 소셜 로그인
            request.socialAccount != null -> {
                try {
                    userService.getUserBySocialId(
                        request.socialAccount.provider,
                        request.socialAccount.socialId
                    )
                } catch (e: Exception) {
                    throw IllegalArgumentException("소셜 로그인 사용자를 찾을 수 없습니다")
                }
            }
            // 이메일 로그인
            !request.email.isNullOrBlank() -> {
                try {
                    userService.getUserByEmail(request.email)
                } catch (e: Exception) {
                    throw IllegalArgumentException("사용자를 찾을 수 없습니다")
                }
            }
            else -> throw IllegalArgumentException("이메일 또는 소셜 계정 정보가 필요합니다")
        }
        
        // 사용자 상태 확인
        if (user.state != com.example.demo.domain.UserState.ACTIVE) {
            throw IllegalArgumentException("비활성화된 사용자입니다")
        }
        
        // 마지막 로그인 시간 업데이트
        userService.updateLastLogin(user.id!!)
        
        // 플랫폼 결정
        val platform = when {
            request.socialAccount != null -> {
                when (request.socialAccount.provider) {
                    com.example.demo.domain.SocialProvider.KAKAO -> TokenPlatform.KAKAO
                    com.example.demo.domain.SocialProvider.NAVER -> TokenPlatform.NAVER
                    com.example.demo.domain.SocialProvider.APPLE -> TokenPlatform.APPLE
                    else -> TokenPlatform.INTERNAL
                }
            }
            else -> TokenPlatform.INTERNAL
        }
        
        // User-Agent 가져오기
        val userAgent = httpRequest.getHeader("User-Agent") ?: "Unknown"
        
        // 토큰 생성 및 저장
        val token = accessTokenService.generateAndSaveToken(user.id!!, platform, userAgent)
        val tokenHash = generateTokenHash(token)
        
        return ResponseEntity.ok(
            TokenResponse(
                accessToken = tokenHash // 해시 값을 accessToken으로 반환
            )
        )
    }
    
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 토큰을 무효화합니다")
    fun logout(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Void> {
        val tokenHash = authHeader.removePrefix("Bearer ")
        accessTokenService.revokeTokenByHash(tokenHash)
        return ResponseEntity.ok().build()
    }
    
    @PostMapping("/logout-all")
    @Operation(summary = "전체 로그아웃", description = "사용자의 모든 토큰을 무효화합니다")
    fun logoutAll(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Void> {
        val tokenHash = authHeader.removePrefix("Bearer ")
        val userId = accessTokenService.getUserIdFromTokenHash(tokenHash)
            ?: throw IllegalArgumentException("유효하지 않은 토큰입니다")
        
        accessTokenService.revokeAllUserTokens(userId)
        return ResponseEntity.ok().build()
    }
    
    @PostMapping("/validate")
    @Operation(summary = "토큰 검증", description = "토큰 해시의 유효성을 검증합니다")
    fun validateToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Map<String, Any?>> {
        val tokenHash = authHeader.removePrefix("Bearer ")
        val isValid = accessTokenService.validateTokenByHash(tokenHash)
        val userId = if (isValid) accessTokenService.getUserIdFromTokenHash(tokenHash) else null
        
        return ResponseEntity.ok(mapOf(
            "valid" to isValid,
            "userId" to userId
        ))
    }
    
    @PostMapping("/apple/login")
    @Operation(summary = "애플 로그인", description = "애플 Identity Token을 검증하고 로그인 처리합니다")
    fun appleLogin(
        @Valid @RequestBody request: AppleLoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<TokenResponse> {
        // 애플 Identity Token 검증
        val validation = appleTokenValidationService.validateAppleToken(request.identityToken)
        
        if (!validation.isValid) {
            throw IllegalArgumentException("유효하지 않은 애플 토큰입니다: ${validation.error}")
        }
        
        val appleSub = validation.sub ?: throw IllegalArgumentException("애플 토큰에 subject가 없습니다")
        val email = validation.email ?: request.user?.email
        
        // 닉네임 결정 (사용자 정보에서 추출)
        val nickname = request.user?.name?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() }
            ?: email?.split("@")?.get(0) ?: "애플사용자"
        
        // 기존 사용자 확인 및 처리
        val user = getOrCreateUserForApple(appleSub, email, nickname)
        
        // 사용자 상태 확인
        if (user.state != com.example.demo.domain.UserState.ACTIVE) {
            throw IllegalArgumentException("비활성화된 사용자입니다")
        }
        
        // 마지막 로그인 시간 업데이트
        userService.updateLastLogin(user.id!!)
        
        // User-Agent 가져오기
        val userAgent = httpRequest.getHeader("User-Agent") ?: "Unknown"
        
        // 토큰 생성 및 저장
        val token = accessTokenService.generateAndSaveToken(user.id!!, TokenPlatform.APPLE, userAgent)
        val tokenHash = generateTokenHash(token)
        
        return ResponseEntity.ok(
            TokenResponse(
                accessToken = tokenHash
            )
        )
    }
    
    @PostMapping("/apple/validate")
    @Operation(summary = "애플 토큰 검증", description = "애플 Identity Token의 유효성을 검증합니다")
    fun validateAppleToken(@RequestBody request: Map<String, String>): ResponseEntity<AppleTokenValidationResponse> {
        val identityToken = request["identityToken"] 
            ?: throw IllegalArgumentException("identityToken이 필요합니다")
        
        val validation = appleTokenValidationService.validateAppleToken(identityToken)
        return ResponseEntity.ok(validation)
    }
    
    @PostMapping("/apple/revoke")
    @Operation(summary = "애플 토큰 무효화", description = "애플 토큰을 무효화합니다")
    fun revokeAppleToken(@RequestBody request: AppleTokenRevocationRequest): ResponseEntity<Map<String, Any>> {
        val success = appleTokenRevokeService.revokeToken(request.token, request.tokenTypeHint)
        
        return ResponseEntity.ok(mapOf(
            "success" to success,
            "message" to if (success) "토큰이 성공적으로 무효화되었습니다" else "토큰 무효화에 실패했습니다"
        ))
    }
    
    @PostMapping("/kakao/login")
    @Operation(summary = "카카오 로그인", description = "카카오 Access Token을 검증하고 로그인 처리합니다")
    fun kakaoLogin(
        @Valid @RequestBody request: KakaoLoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<TokenResponse> {
        // 카카오 Access Token 검증
        val validation = kakaoTokenValidationService.validateKakaoToken(request.accessToken)
        
        if (!validation.isValid) {
            throw IllegalArgumentException("유효하지 않은 카카오 토큰입니다: ${validation.error}")
        }
        
        val kakaoUserInfo = validation.userInfo ?: throw IllegalArgumentException("카카오 사용자 정보를 가져올 수 없습니다")
        val kakaoSub = kakaoUserInfo.id.toString()
        
        // 닉네임 결정 (카카오 계정 정보에서 추출)
        val nickname = kakaoUserInfo.kakaoAccount?.profile?.nickname
            ?: kakaoUserInfo.properties?.nickname
            ?: "카카오사용자${kakaoUserInfo.id}"
        
        // 이메일 결정 (카카오 계정 정보에서 추출)
        val email = kakaoUserInfo.kakaoAccount?.email
            ?: "${kakaoUserInfo.id}@kakao.local"
        
        // 기존 사용자 확인 및 처리
        val user = getOrCreateUserForKakao(kakaoSub, email, nickname, request.accessToken)
        
        // 사용자 상태 확인
        if (user.state != com.example.demo.domain.UserState.ACTIVE) {
            throw IllegalArgumentException("비활성화된 사용자입니다")
        }
        
        // 마지막 로그인 시간 업데이트
        userService.updateLastLogin(user.id!!)
        
        // User-Agent 가져오기
        val userAgent = httpRequest.getHeader("User-Agent") ?: "Unknown"
        
        // 토큰 생성 및 저장
        val token = accessTokenService.generateAndSaveToken(user.id!!, TokenPlatform.KAKAO, userAgent)
        val tokenHash = generateTokenHash(token)
        
        return ResponseEntity.ok(
            TokenResponse(
                accessToken = tokenHash
            )
        )
    }
    
    @PostMapping("/kakao/validate")
    @Operation(summary = "카카오 토큰 검증", description = "카카오 Access Token의 유효성을 검증합니다")
    fun validateKakaoToken(@RequestBody request: Map<String, String>): ResponseEntity<KakaoTokenValidationResponse> {
        val accessToken = request["accessToken"] 
            ?: throw IllegalArgumentException("accessToken이 필요합니다")
        
        val validation = kakaoTokenValidationService.validateKakaoToken(accessToken)
        return ResponseEntity.ok(validation)
    }
    
    @PostMapping("/kakao/logout")
    @Operation(summary = "카카오 로그아웃", description = "카카오 토큰을 무효화합니다")
    fun logoutKakao(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val accessToken = request["accessToken"] 
            ?: throw IllegalArgumentException("accessToken이 필요합니다")
        
        val success = kakaoTokenValidationService.logoutKakao(accessToken)
        
        return ResponseEntity.ok(mapOf(
            "success" to success,
            "message" to if (success) "카카오 로그아웃이 성공적으로 처리되었습니다" else "카카오 로그아웃에 실패했습니다"
        ))
    }
    
    @PostMapping("/kakao/unlink")
    @Operation(summary = "카카오 연결 끊기", description = "카카오 앱 연결을 끊습니다")
    fun unlinkKakao(@RequestBody request: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val accessToken = request["accessToken"] 
            ?: throw IllegalArgumentException("accessToken이 필요합니다")
        
        val success = kakaoTokenValidationService.unlinkKakao(accessToken)
        
        return ResponseEntity.ok(mapOf(
            "success" to success,
            "message" to if (success) "카카오 연결 끊기가 성공적으로 처리되었습니다" else "카카오 연결 끊기에 실패했습니다"
        ))
    }
    
    /**
     * 카카오 로그인 시 사용자 확인 및 생성 처리
     */
    private fun getOrCreateUserForKakao(kakaoSub: String, email: String, nickname: String, accessToken: String): com.example.demo.domain.User {
        // 1. 카카오 소셜 계정 존재 여부 확인
        if (userService.existsBySocialId(com.example.demo.domain.SocialProvider.KAKAO, kakaoSub)) {
            // 이미 카카오로 가입된 사용자 - 로그인 처리
            return userService.getUserBySocialId(com.example.demo.domain.SocialProvider.KAKAO, kakaoSub)
        }
        
        // 2. 새로운 사용자 회원가입
        val signupRequest = UserSignupRequest(
            email = email,
            nickname = nickname,
            socialAccount = SocialAccountRequest(
                provider = com.example.demo.domain.SocialProvider.KAKAO,
                socialId = kakaoSub,
                email = email,
                accessToken = accessToken
            )
        )
        
        userService.signup(signupRequest)
        return userService.getUserByEmail(email)
    }
    
    /**
     * 애플 로그인 시 사용자 확인 및 생성 처리
     */
    private fun getOrCreateUserForApple(appleSub: String, email: String?, nickname: String): com.example.demo.domain.User {
        // 1. 애플 소셜 계정 존재 여부 확인
        if (userService.existsBySocialId(com.example.demo.domain.SocialProvider.APPLE, appleSub)) {
            // 이미 애플로 가입된 사용자 - 로그인 처리
            return userService.getUserBySocialId(com.example.demo.domain.SocialProvider.APPLE, appleSub)
        }
        
        // 이메일이 없으면 새 사용자 생성 불가
        if (email.isNullOrBlank()) {
            throw IllegalArgumentException("이메일 정보가 필요합니다")
        }
        
        // 2. 새로운 사용자 회원가입
        val signupRequest = UserSignupRequest(
            email = email,
            nickname = nickname,
            socialAccount = SocialAccountRequest(
                provider = com.example.demo.domain.SocialProvider.APPLE,
                socialId = appleSub,
                email = email
            )
        )
        
        userService.signup(signupRequest)
        return userService.getUserByEmail(email)
    }

    private fun generateTokenHash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
} 