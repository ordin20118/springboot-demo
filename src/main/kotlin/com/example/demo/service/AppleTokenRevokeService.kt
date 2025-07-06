package com.example.demo.service

import com.example.demo.config.AppleLoginConfig
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class AppleTokenRevokeService(
    private val appleLoginConfig: AppleLoginConfig,
    private val appleTokenValidationService: AppleTokenValidationService,
    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://appleid.apple.com")
        .build()
) {
    
    private val logger = LoggerFactory.getLogger(AppleTokenRevokeService::class.java)

    /**
     * 애플 토큰 무효화
     */
    fun revokeToken(token: String, tokenType: String = "refresh_token"): Boolean {
        return try {
            val clientSecret = appleTokenValidationService.generateClientSecret()
            
            val requestBody = mapOf(
                "client_id" to appleLoginConfig.bundleId,
                "client_secret" to clientSecret,
                "token" to token,
                "token_type_hint" to tokenType
            )
            
            val response = webClient.post()
                .uri("/auth/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(requestBody.entries.fold(
                    org.springframework.util.LinkedMultiValueMap<String, String>()
                ) { map, entry ->
                    map.add(entry.key, entry.value)
                    map
                }))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(10))
                .block()
            
            val success = response?.statusCode?.is2xxSuccessful ?: false
            if (success) {
                logger.info("Successfully revoked Apple token")
            } else {
                logger.warn("Failed to revoke Apple token, status: ${response?.statusCode}")
            }
            
            success
        } catch (e: Exception) {
            logger.error("Error revoking Apple token", e)
            false
        }
    }
    
    /**
     * 사용자의 모든 애플 토큰 무효화
     */
    fun revokeAllUserTokens(userId: Long): Boolean {
        // 실제 구현에서는 데이터베이스에서 해당 사용자의 모든 애플 토큰을 가져와서 무효화
        // 현재는 로그만 남김
        logger.info("Revoking all Apple tokens for user: $userId")
        return true
    }
} 