package com.example.demo.controller

import com.example.demo.dto.AppleServerToServerNotification
import com.example.demo.service.AppleWebhookService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/apple/webhook")
@Tag(name = "Apple Webhook", description = "애플 Server-to-Server 웹훅 API")
class AppleWebhookController(
    private val appleWebhookService: AppleWebhookService
) {
    
    private val logger = LoggerFactory.getLogger(AppleWebhookController::class.java)

    @PostMapping("/notification")
    @Operation(summary = "애플 Server-to-Server 알림 수신", description = "애플에서 보내는 사용자 계정 상태 변경 알림을 처리합니다")
    fun handleAppleNotification(@RequestBody notification: AppleServerToServerNotification): ResponseEntity<Void> {
        return try {
            logger.info("Received Apple server-to-server notification")
            appleWebhookService.processNotification(notification)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Failed to process Apple notification", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
} 