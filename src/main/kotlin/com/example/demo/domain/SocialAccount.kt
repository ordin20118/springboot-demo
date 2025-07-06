package com.example.demo.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "social_accounts",
    uniqueConstraints = [
        UniqueConstraint(name = "uniq_social", columnNames = ["provider", "social_id"])
    ]
)
data class SocialAccount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    val user: User,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    val provider: SocialProvider,
    
    @Column(name = "social_id", length = 200, nullable = false)
    val socialId: String,
    
    @Column(name = "email", length = 150)
    val email: String? = null,
    
    @Column(name = "access_token", columnDefinition = "TEXT")
    val accessToken: String? = null,
    
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    val refreshToken: String? = null,
    
    @Column(name = "connected_at", nullable = false)
    val connectedAt: LocalDateTime = LocalDateTime.now()
)

enum class SocialProvider(val displayName: String) {
    APPLE("애플"),
    GOOGLE("구글"),
    KAKAO("카카오"),
    NAVER("네이버")
} 