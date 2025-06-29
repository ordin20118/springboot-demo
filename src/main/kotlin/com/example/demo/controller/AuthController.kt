package com.example.demo.controller

import com.example.demo.dto.TokenResponse
import com.example.demo.dto.UserLoginRequest
import com.example.demo.dto.UserSignupRequest
import com.example.demo.dto.UserResponse
import com.example.demo.security.JwtTokenProvider
import com.example.demo.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "사용자 인증 관련 API")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    fun signup(@Valid @RequestBody request: UserSignupRequest): ResponseEntity<UserResponse> {
        val userResponse = userService.signup(request)
        return ResponseEntity.ok(userResponse)
    }
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인 후 JWT 토큰을 반환합니다")
    fun login(@Valid @RequestBody request: UserLoginRequest): ResponseEntity<TokenResponse> {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )
        
        val jwt = jwtTokenProvider.generateToken(authentication)
        return ResponseEntity.ok(TokenResponse(accessToken = jwt))
    }
} 