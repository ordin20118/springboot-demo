package com.example.demo.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

// 애플 로그인 요청 DTO
data class AppleLoginRequest(
    @field:NotBlank(message = "Identity Token은 필수입니다")
    val identityToken: String,
    
    @field:NotBlank(message = "Authorization Code는 필수입니다")
    val authorizationCode: String,
    
    val user: AppleUserInfo? = null,
    
    val state: String? = null
)

// 애플 사용자 정보 DTO
data class AppleUserInfo(
    val name: AppleUserName? = null,
    val email: String? = null
)

// 애플 사용자 이름 DTO
data class AppleUserName(
    val firstName: String? = null,
    val lastName: String? = null
)

// 애플 토큰 검증 응답 DTO
data class AppleTokenValidationResponse(
    val isValid: Boolean,
    val sub: String? = null,
    val email: String? = null,
    val emailVerified: Boolean? = null,
    val iss: String? = null,
    val aud: String? = null,
    val exp: Long? = null,
    val iat: Long? = null,
    val authTime: Long? = null,
    val nonce: String? = null,
    val nonceSupported: Boolean? = null,
    val error: String? = null
)

// 애플 공개 키 정보 DTO
data class ApplePublicKeys(
    val keys: List<ApplePublicKey>
)

data class ApplePublicKey(
    val kty: String,
    val kid: String,
    val use: String,
    val alg: String,
    val n: String,
    val e: String
)

// 애플 Server-to-Server 알림 DTO
data class AppleServerToServerNotification(
    @JsonProperty("signed_payload")
    val signedPayload: String
)

// 애플 알림 페이로드 DTO
data class AppleNotificationPayload(
    val iss: String,
    val aud: String,
    val iat: Long,
    val exp: Long,
    val sub: String,
    val events: Map<String, Any>
)

// 애플 토큰 폐기 요청 DTO
data class AppleTokenRevocationRequest(
    @field:NotBlank(message = "Token은 필수입니다")
    val token: String,
    
    val tokenTypeHint: String = "refresh_token"
)

// 애플 사용자 탈퇴 요청 DTO
data class AppleUserDeletionRequest(
    @field:NotBlank(message = "사용자 ID는 필수입니다")
    val userId: String,
    
    val reason: String? = null
) 