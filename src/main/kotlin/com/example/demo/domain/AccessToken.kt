package com.example.demo.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "access_tokens",
    uniqueConstraints = [
        UniqueConstraint(name = "uniq_hash", columnNames = ["token_hash"])
    ],
    indexes = [
        Index(name = "idx_user_platform", columnList = "user_id, platform")
    ]
)
data class AccessToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    val tokenId: Long? = null,
    
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    
    @Column(name = "token_hash", length = 200, nullable = false)
    val tokenHash: String,
    
    @Column(name = "token", columnDefinition = "TEXT")
    val token: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    val platform: TokenPlatform,
    
    @Column(name = "user_agent", length = 300, nullable = false)
    val userAgent: String,
    
    @Column(name = "expire_date", nullable = false)
    val expireDate: LocalDateTime,
    
    @Column(name = "revoked_at")
    val revokedAt: LocalDateTime? = null,
    
    @Column(name = "reg_date", nullable = false)
    val regDate: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "update_date")
    val updateDate: LocalDateTime = LocalDateTime.now()
)

enum class TokenPlatform(val displayName: String) {
    INTERNAL("내부"),
    KAKAO("카카오"),
    NAVER("네이버"),
    APPLE("애플")
}
