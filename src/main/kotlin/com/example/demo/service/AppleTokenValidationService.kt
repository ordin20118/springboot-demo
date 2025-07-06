package com.example.demo.service

import com.example.demo.config.AppleLoginConfig
import com.example.demo.dto.ApplePublicKeys
import com.example.demo.dto.AppleTokenValidationResponse
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.time.Instant
import java.util.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Service
class AppleTokenValidationService(
    private val appleLoginConfig: AppleLoginConfig
) {
    
    private val webClient: WebClient by lazy {
        WebClient.builder()
            .baseUrl(appleLoginConfig.authKeysUrl)
            .build()
    }

    private val logger = LoggerFactory.getLogger(AppleTokenValidationService::class.java)

    /**
     * 애플 Identity Token 검증
     */
    fun validateAppleToken(identityToken: String): AppleTokenValidationResponse {
        try {
            // JWT 파싱
            val jwt = JWTParser.parse(identityToken)
            
            if (jwt !is SignedJWT) {
                return AppleTokenValidationResponse(
                    isValid = false,
                    error = "Token is not a signed JWT"
                )
            }

            // 헤더에서 kid 추출
            val kid = jwt.header.keyID
            if (kid.isNullOrEmpty()) {
                return AppleTokenValidationResponse(
                    isValid = false,
                    error = "Token header does not contain kid"
                )
            }

            // 애플 공개 키 가져오기
            val publicKey = getApplePublicKey(kid)
            if (publicKey == null) {
                return AppleTokenValidationResponse(
                    isValid = false,
                    error = "Cannot find public key for kid: $kid"
                )
            }

            // 서명 검증
            val verifier = com.nimbusds.jose.crypto.RSASSAVerifier(publicKey)
            if (!jwt.verify(verifier)) {
                return AppleTokenValidationResponse(
                    isValid = false,
                    error = "Token signature verification failed"
                )
            }

            // 클레임 검증
            val claims = jwt.jwtClaimsSet
            val validationResult = validateClaims(claims)
            
            if (!validationResult.isValid) {
                return validationResult
            }

            // 성공적으로 검증된 경우
            return AppleTokenValidationResponse(
                isValid = true,
                sub = claims.subject,
                email = claims.getStringClaim("email"),
                emailVerified = claims.getBooleanClaim("email_verified"),
                iss = claims.issuer,
                aud = claims.audience?.firstOrNull(),
                exp = claims.expirationTime?.time?.div(1000),
                iat = claims.issueTime?.time?.div(1000),
                authTime = claims.getLongClaim("auth_time"),
                nonce = claims.getStringClaim("nonce"),
                nonceSupported = claims.getBooleanClaim("nonce_supported")
            )

        } catch (e: Exception) {
            logger.error("Apple token validation failed", e)
            return AppleTokenValidationResponse(
                isValid = false,
                error = "Token validation failed: ${e.message}"
            )
        }
    }

    /**
     * 애플 공개 키 가져오기 (캐시 적용)
     */
    @Cacheable(value = ["applePublicKeys"], key = "#kid")
    fun getApplePublicKey(kid: String): RSAPublicKey? {
        return try {
            val response = webClient.get()
                .uri(appleLoginConfig.authKeysUrl)
                .retrieve()
                .bodyToMono(ApplePublicKeys::class.java)
                .timeout(Duration.ofSeconds(10))
                .block()

            response?.keys?.find { it.kid == kid }?.let { key ->
                val jwk = RSAKey.Builder(
                    com.nimbusds.jose.util.Base64URL(key.n),
                    com.nimbusds.jose.util.Base64URL(key.e)
                ).keyID(key.kid).build()
                
                jwk.toRSAPublicKey()
            }
        } catch (e: Exception) {
            logger.error("Failed to fetch Apple public key for kid: $kid", e)
            null
        }
    }

    /**
     * JWT 클레임 검증
     */
    private fun validateClaims(claims: JWTClaimsSet): AppleTokenValidationResponse {
        val now = Instant.now()
        
        // issuer 검증
        if (claims.issuer != appleLoginConfig.issuer) {
            return AppleTokenValidationResponse(
                isValid = false,
                error = "Invalid issuer: ${claims.issuer}"
            )
        }

        // audience 검증
        val audience = claims.audience?.firstOrNull()
        if (audience != appleLoginConfig.bundleId) {
            return AppleTokenValidationResponse(
                isValid = false,
                error = "Invalid audience: $audience"
            )
        }

        // 만료 시간 검증
        val expirationTime = claims.expirationTime
        if (expirationTime == null || expirationTime.toInstant().isBefore(now)) {
            return AppleTokenValidationResponse(
                isValid = false,
                error = "Token has expired"
            )
        }

        // 발행 시간 검증 (너무 오래된 토큰 거부)
        val issueTime = claims.issueTime
        if (issueTime == null || issueTime.toInstant().isBefore(now.minusSeconds(3600))) {
            return AppleTokenValidationResponse(
                isValid = false,
                error = "Token is too old"
            )
        }

        // subject 검증
        if (claims.subject.isNullOrEmpty()) {
            return AppleTokenValidationResponse(
                isValid = false,
                error = "Token does not contain subject"
            )
        }

        return AppleTokenValidationResponse(isValid = true)
    }

    /**
     * 애플 공개 키 캐시 갱신
     */
    fun refreshApplePublicKeys(): Mono<ApplePublicKeys> {
        return webClient.get()
            .uri(appleLoginConfig.authKeysUrl)
            .retrieve()
            .bodyToMono(ApplePublicKeys::class.java)
            .timeout(Duration.ofSeconds(10))
            .doOnSuccess { logger.info("Apple public keys refreshed successfully") }
            .doOnError { logger.error("Failed to refresh Apple public keys", it) }
    }

    /**
     * 클라이언트 시크릿 생성 (애플 API 호출시 필요)
     */
    fun generateClientSecret(): String {
        return try {
            val privateKey = loadPrivateKey()
            val now = Instant.now()
            
            val claims = JWTClaimsSet.Builder()
                .issuer(appleLoginConfig.teamId)
                .subject(appleLoginConfig.bundleId)
                .audience("https://appleid.apple.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600))) // 1시간 유효
                .build()
            
            val jwt = SignedJWT(
                com.nimbusds.jose.JWSHeader.Builder(com.nimbusds.jose.JWSAlgorithm.ES256)
                    .keyID(appleLoginConfig.keyId)
                    .build(),
                claims
            )
            
            jwt.sign(com.nimbusds.jose.crypto.ECDSASigner(privateKey))
            jwt.serialize()
        } catch (e: Exception) {
            logger.error("Failed to generate client secret", e)
            throw RuntimeException("클라이언트 시크릿 생성 실패", e)
        }
    }
    
    /**
     * 애플 개발자 키 파일에서 private key 로드
     */
    private fun loadPrivateKey(): java.security.interfaces.ECPrivateKey {
        return try {
            val keyPath = appleLoginConfig.privateKeyPath
            val keyFile = if (keyPath.startsWith("/")) {
                File(keyPath)
            } else {
                // 프로젝트 루트 경로에서 찾기
                File(System.getProperty("user.dir"), keyPath)
            }
            
            if (!keyFile.exists()) {
                throw RuntimeException("애플 개발자 키 파일을 찾을 수 없습니다: ${keyFile.absolutePath}")
            }
            
            val keyContent = Files.readString(keyFile.toPath())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\n", "")
                .replace("\r", "")
            
            val keyBytes = Base64.getDecoder().decode(keyContent)
            val keySpec = java.security.spec.PKCS8EncodedKeySpec(keyBytes)
            val keyFactory = java.security.KeyFactory.getInstance("EC")
            
            keyFactory.generatePrivate(keySpec) as java.security.interfaces.ECPrivateKey
        } catch (e: Exception) {
            logger.error("Failed to load private key from file: ${appleLoginConfig.privateKeyPath}", e)
            throw RuntimeException("개발자 키 로드 실패", e)
        }
    }
} 