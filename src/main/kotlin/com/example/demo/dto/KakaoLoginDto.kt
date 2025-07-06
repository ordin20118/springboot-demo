package com.example.demo.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

// 카카오 로그인 요청 DTO
data class KakaoLoginRequest(
    @field:NotBlank(message = "Access Token은 필수입니다")
    val accessToken: String
)

// 카카오 사용자 정보 응답 DTO
data class KakaoUserInfoResponse(
    val id: Long,
    @JsonProperty("connected_at")
    val connectedAt: String?,
    val properties: KakaoProperties?,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?
)

// 카카오 사용자 속성 DTO
data class KakaoProperties(
    val nickname: String?
)

// 카카오 계정 정보 DTO
data class KakaoAccount(
    @JsonProperty("profile_needs_agreement")
    val profileNeedsAgreement: Boolean?,
    @JsonProperty("profile_nickname_needs_agreement")
    val profileNicknameNeedsAgreement: Boolean?,
    @JsonProperty("profile_image_needs_agreement")
    val profileImageNeedsAgreement: Boolean?,
    val profile: KakaoProfile?,
    @JsonProperty("name_needs_agreement")
    val nameNeedsAgreement: Boolean?,
    val name: String?,
    @JsonProperty("email_needs_agreement")
    val emailNeedsAgreement: Boolean?,
    @JsonProperty("is_email_valid")
    val isEmailValid: Boolean?,
    @JsonProperty("is_email_verified")
    val isEmailVerified: Boolean?,
    val email: String?,
    @JsonProperty("age_range_needs_agreement")
    val ageRangeNeedsAgreement: Boolean?,
    @JsonProperty("age_range")
    val ageRange: String?,
    @JsonProperty("birthyear_needs_agreement")
    val birthyearNeedsAgreement: Boolean?,
    val birthyear: String?,
    @JsonProperty("birthday_needs_agreement")
    val birthdayNeedsAgreement: Boolean?,
    val birthday: String?,
    @JsonProperty("birthday_type")
    val birthdayType: String?,
    @JsonProperty("gender_needs_agreement")
    val genderNeedsAgreement: Boolean?,
    val gender: String?,
    @JsonProperty("phone_number_needs_agreement")
    val phoneNumberNeedsAgreement: Boolean?,
    @JsonProperty("phone_number")
    val phoneNumber: String?,
    @JsonProperty("ci_needs_agreement")
    val ciNeedsAgreement: Boolean?,
    val ci: String?,
    @JsonProperty("ci_authenticated_at")
    val ciAuthenticatedAt: String?
)

// 카카오 프로필 DTO
data class KakaoProfile(
    val nickname: String?,
    @JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String?,
    @JsonProperty("profile_image_url")
    val profileImageUrl: String?,
    @JsonProperty("is_default_image")
    val isDefaultImage: Boolean?
)

// 카카오 토큰 검증 응답 DTO
data class KakaoTokenValidationResponse(
    val isValid: Boolean,
    val userInfo: KakaoUserInfoResponse? = null,
    val error: String? = null
)

// 카카오 토큰 정보 조회 응답 DTO
data class KakaoTokenInfoResponse(
    val id: Long,
    @JsonProperty("expires_in")
    val expiresIn: Int?,
    @JsonProperty("app_id")
    val appId: Int?
)

// 카카오 에러 응답 DTO
data class KakaoErrorResponse(
    val msg: String,
    val code: Int
) 