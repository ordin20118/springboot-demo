package com.example.demo.service

import com.example.demo.config.KakaoLoginConfig
import com.example.demo.dto.KakaoTokenValidationResponse
import com.example.demo.dto.KakaoUserInfoResponse
import com.example.demo.dto.KakaoErrorResponse
import com.example.demo.dto.KakaoTokenInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class KakaoTokenValidationService(
    private val kakaoLoginConfig: KakaoLoginConfig
) {
    
    private val kakaoWebClient: WebClient by lazy {
        val baseUrl = "https://kapi.kakao.com"
        logger.info("Creating Kakao WebClient with baseUrl: $baseUrl")
        WebClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
    
    private val logger = LoggerFactory.getLogger(KakaoTokenValidationService::class.java)
    
    /**
     * 카카오 Access Token을 검증하고 사용자 정보를 조회합니다.
     */
    fun validateKakaoToken(accessToken: String): KakaoTokenValidationResponse {
        return try {
            // 1. 토큰 유효성 검증
            val tokenInfo = getTokenInfo(accessToken)
            if (tokenInfo == null) {
                return KakaoTokenValidationResponse(
                    isValid = false,
                    error = "Invalid access token"
                )
            }
            
            // 2. 사용자 정보 조회
            val userInfo = getUserInfo(accessToken)
            if (userInfo == null) {
                return KakaoTokenValidationResponse(
                    isValid = false,
                    error = "Failed to get user info"
                )
            }
            
            KakaoTokenValidationResponse(
                isValid = true,
                userInfo = userInfo
            )
        } catch (e: Exception) {
            logger.error("Failed to validate Kakao token", e)
            KakaoTokenValidationResponse(
                isValid = false,
                error = "Token validation failed: ${e.message}"
            )
        }
    }
    
    /**
     * 카카오 Access Token 정보를 조회합니다.
     */
    private fun getTokenInfo(accessToken: String): KakaoTokenInfoResponse? {
        return try {
            kakaoWebClient.get()
                .uri("/v1/user/access_token_info")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(KakaoTokenInfoResponse::class.java)
                .block()
        } catch (e: WebClientResponseException) {
            logger.error("Failed to get token info from Kakao API", e)
            when (e.statusCode) {
                HttpStatus.UNAUTHORIZED -> {
                    logger.warn("Invalid Kakao access token")
                    null
                }
                else -> {
                    logger.error("Kakao API error: ${e.statusCode} - ${e.responseBodyAsString}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error during token info retrieval", e)
            null
        }
    }
    
    /**
     * 카카오 사용자 정보를 조회합니다.
     */
    private fun getUserInfo(accessToken: String): KakaoUserInfoResponse? {
        return try {
            kakaoWebClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse::class.java)
                .block()
        } catch (e: WebClientResponseException) {
            logger.error("Failed to get user info from Kakao API", e)
            when (e.statusCode) {
                HttpStatus.UNAUTHORIZED -> {
                    logger.warn("Invalid Kakao access token for user info")
                    null
                }
                else -> {
                    logger.error("Kakao API error: ${e.statusCode} - ${e.responseBodyAsString}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error during user info retrieval", e)
            null
        }
    }
    
    /**
     * 카카오 로그아웃 처리 (토큰 무효화)
     */
    fun logoutKakao(accessToken: String): Boolean {
        return try {
            kakaoWebClient.post()
                .uri("/v1/user/logout")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()
            
            true
        } catch (e: WebClientResponseException) {
            logger.error("Failed to logout from Kakao API", e)
            false
        } catch (e: Exception) {
            logger.error("Unexpected error during Kakao logout", e)
            false
        }
    }
    
    /**
     * 카카오 연결 끊기 처리
     */
    fun unlinkKakao(accessToken: String): Boolean {
        return try {
            kakaoWebClient.post()
                .uri("/v1/user/unlink")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()
            
            true
        } catch (e: WebClientResponseException) {
            logger.error("Failed to unlink from Kakao API", e)
            false
        } catch (e: Exception) {
            logger.error("Unexpected error during Kakao unlink", e)
            false
        }
    }
} 