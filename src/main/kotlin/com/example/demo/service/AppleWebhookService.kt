package com.example.demo.service

import com.example.demo.dto.AppleNotificationPayload
import com.example.demo.dto.AppleServerToServerNotification
import com.example.demo.repository.SocialAccountRepository
import com.example.demo.repository.UserRepository
import com.example.demo.domain.SocialProvider
import com.example.demo.domain.UserState
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AppleWebhookService(
    private val userRepository: UserRepository,
    private val socialAccountRepository: SocialAccountRepository,
    private val appleTokenValidationService: AppleTokenValidationService,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(AppleWebhookService::class.java)

    /**
     * 애플 Server-to-Server 알림 처리
     */
    fun processNotification(notification: AppleServerToServerNotification) {
        try {
            // 서명된 페이로드 검증
            val payload = parseAndValidatePayload(notification.signedPayload)
            if (payload == null) {
                logger.warn("Invalid Apple notification payload")
                return
            }

            // 이벤트 처리
            val events = payload.events
            for ((eventType, eventData) in events) {
                when (eventType) {
                    "account-delete" -> handleAccountDeletion(payload.sub, eventData)
                    "email-disabled" -> handleEmailDisabled(payload.sub, eventData)
                    "email-enabled" -> handleEmailEnabled(payload.sub, eventData)
                    "consent-withdrawn" -> handleConsentWithdrawn(payload.sub, eventData)
                    else -> logger.info("Unhandled event type: $eventType")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to process Apple notification", e)
            throw e
        }
    }

    /**
     * 서명된 페이로드 파싱 및 검증
     */
    private fun parseAndValidatePayload(signedPayload: String): AppleNotificationPayload? {
        return try {
            // JWT 파싱
            val jwt = JWTParser.parse(signedPayload)
            if (jwt !is SignedJWT) {
                logger.warn("Apple notification payload is not a signed JWT")
                return null
            }

            // 검증은 실제 환경에서 애플 공개 키로 수행해야 함
            // 현재는 클레임만 추출
            val claims = jwt.jwtClaimsSet
            
            AppleNotificationPayload(
                iss = claims.issuer,
                aud = claims.audience?.firstOrNull() ?: "",
                iat = claims.issueTime?.time?.div(1000) ?: 0,
                exp = claims.expirationTime?.time?.div(1000) ?: 0,
                sub = claims.subject,
                events = claims.getJSONObjectClaim("events") as? Map<String, Any> ?: emptyMap()
            )
        } catch (e: Exception) {
            logger.error("Failed to parse Apple notification payload", e)
            null
        }
    }

    /**
     * 계정 삭제 처리
     */
    private fun handleAccountDeletion(appleSub: String, eventData: Any) {
        try {
            logger.info("Processing account deletion for Apple user: $appleSub")
            
            // 애플 소셜 계정 찾기
            val socialAccount = socialAccountRepository.findByProviderAndSocialId(
                SocialProvider.APPLE, appleSub
            ).orElse(null)
            
            if (socialAccount != null) {
                val user = socialAccount.user
                
                // 사용자 상태를 탈퇴로 변경 (User 엔티티는 data class이므로 copy 사용)
                val updatedUser = user.copy(
                    state = UserState.WITHDRAWN,
                    withdrawDate = java.time.LocalDateTime.now()
                )
                userRepository.save(updatedUser)
                
                // 소셜 계정 삭제
                socialAccountRepository.delete(socialAccount)
                
                logger.info("Successfully processed account deletion for user: ${user.id}")
            } else {
                logger.warn("Apple social account not found for sub: $appleSub")
            }
        } catch (e: Exception) {
            logger.error("Failed to handle account deletion for Apple user: $appleSub", e)
        }
    }

    /**
     * 이메일 비활성화 처리
     */
    private fun handleEmailDisabled(appleSub: String, eventData: Any) {
        try {
            logger.info("Processing email disabled for Apple user: $appleSub")
            
            val socialAccount = socialAccountRepository.findByProviderAndSocialId(
                SocialProvider.APPLE, appleSub
            ).orElse(null)
            
            if (socialAccount != null) {
                // 소셜 계정의 이메일 정보 제거 (SocialAccount 엔티티는 data class이므로 copy 사용)
                val updatedSocialAccount = socialAccount.copy(email = null)
                socialAccountRepository.save(updatedSocialAccount)
                
                logger.info("Successfully processed email disabled for user: ${socialAccount.user.id}")
            } else {
                logger.warn("Apple social account not found for sub: $appleSub")
            }
        } catch (e: Exception) {
            logger.error("Failed to handle email disabled for Apple user: $appleSub", e)
        }
    }

    /**
     * 이메일 활성화 처리
     */
    private fun handleEmailEnabled(appleSub: String, eventData: Any) {
        try {
            logger.info("Processing email enabled for Apple user: $appleSub")
            
            val socialAccount = socialAccountRepository.findByProviderAndSocialId(
                SocialProvider.APPLE, appleSub
            ).orElse(null)
            
            if (socialAccount != null) {
                // 이메일 정보 업데이트는 별도 API 호출이 필요할 수 있음
                logger.info("Email enabled notification received for user: ${socialAccount.user.id}")
            } else {
                logger.warn("Apple social account not found for sub: $appleSub")
            }
        } catch (e: Exception) {
            logger.error("Failed to handle email enabled for Apple user: $appleSub", e)
        }
    }

    /**
     * 동의 철회 처리
     */
    private fun handleConsentWithdrawn(appleSub: String, eventData: Any) {
        try {
            logger.info("Processing consent withdrawn for Apple user: $appleSub")
            
            val socialAccount = socialAccountRepository.findByProviderAndSocialId(
                SocialProvider.APPLE, appleSub
            ).orElse(null)
            
            if (socialAccount != null) {
                val user = socialAccount.user
                
                // 마케팅 동의 철회 (User 엔티티는 data class이므로 copy 사용)
                val updatedUser = user.copy(
                    marketing = com.example.demo.domain.ConsentType.DISAGREE
                )
                userRepository.save(updatedUser)
                
                logger.info("Successfully processed consent withdrawn for user: ${user.id}")
            } else {
                logger.warn("Apple social account not found for sub: $appleSub")
            }
        } catch (e: Exception) {
            logger.error("Failed to handle consent withdrawn for Apple user: $appleSub", e)
        }
    }
} 