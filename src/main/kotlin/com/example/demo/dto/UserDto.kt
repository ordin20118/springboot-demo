package com.example.demo.dto

import com.example.demo.domain.UserState
import com.example.demo.domain.Gender
import com.example.demo.domain.ConsentType
import com.example.demo.domain.SocialProvider
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Max
import java.time.LocalDateTime

data class UserSignupRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,
    
    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
    val nickname: String,
    
    @field:Size(max = 500, message = "프로필은 500자 이하여야 합니다")
    val profile: String? = null,
    
    @field:Min(value = 0, message = "나이는 0 이상이어야 합니다")
    @field:Max(value = 150, message = "나이는 150 이하여야 합니다")
    val age: Int = 0,
    
    val gender: Gender = Gender.NONE,
    
    val marketing: ConsentType = ConsentType.DISAGREE,
    val termsOfService: ConsentType = ConsentType.DISAGREE,
    val personalInfoPolicy: ConsentType = ConsentType.DISAGREE,
    
    // 소셜 로그인 정보
    val socialAccount: SocialAccountRequest? = null
)

data class SocialAccountRequest(
    val provider: SocialProvider,
    val socialId: String,
    val email: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null
)

data class UserLoginRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String? = null,
    
    // 소셜 로그인용
    val socialAccount: SocialAccountRequest? = null
)

data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val profile: String?,
    val age: Int,
    val gender: Gender,
    val state: UserState,
    val lastLogin: LocalDateTime?,
    val marketing: ConsentType,
    val termsOfService: ConsentType,
    val personalInfoPolicy: ConsentType,
    val regDate: LocalDateTime,
    val updateDate: LocalDateTime,
    val socialAccounts: List<SocialAccountResponse> = emptyList()
)

data class SocialAccountResponse(
    val id: Long,
    val provider: SocialProvider,
    val socialId: String,
    val email: String?,
    val connectedAt: LocalDateTime
)

data class UserUpdateRequest(
    @field:Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
    val nickname: String? = null,
    
    @field:Min(value = 0, message = "나이는 0 이상이어야 합니다")
    @field:Max(value = 150, message = "나이는 150 이하여야 합니다")
    val age: Int? = null,
    
    val gender: Gender? = null,
    
    @field:Size(max = 500, message = "프로필은 500자 이하여야 합니다")
    val profile: String? = null,
    
    val marketing: ConsentType? = null
)

data class UserWithdrawRequest(
    @field:Size(max = 500, message = "탈퇴 사유는 500자 이하여야 합니다")
    val withdrawalReason: String? = null
)

data class TokenResponse(
    val accessToken: String, // 해시된 토큰 값
    val tokenType: String = "Bearer"
) 