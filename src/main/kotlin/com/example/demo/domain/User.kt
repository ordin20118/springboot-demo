package com.example.demo.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uniq_email", columnNames = ["email"])
    ],
    indexes = [
        Index(name = "nick_idx", columnList = "nickname")
    ]
)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,
    
    @Column(name = "email", length = 150, nullable = false)
    val email: String,
    
    @Column(name = "nickname", length = 50, nullable = false)
    val nickname: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    val state: UserState = UserState.ACTIVE,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    val gender: Gender = Gender.NONE,
    
    @Column(name = "age", nullable = false)
    val age: Int = 0,
    
    @Column(name = "profile", length = 500)
    val profile: String? = null,
    
    @Column(name = "last_login")
    val lastLogin: LocalDateTime? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "marketing", nullable = false)
    val marketing: ConsentType = ConsentType.DISAGREE,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "terms_of_service", nullable = false)
    val termsOfService: ConsentType = ConsentType.DISAGREE,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "personal_info_policy", nullable = false)
    val personalInfoPolicy: ConsentType = ConsentType.DISAGREE,
    
    @Column(name = "withdrawal_reason", length = 500)
    val withdrawalReason: String? = null,
    
    @Column(name = "reg_date", nullable = false)
    val regDate: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "update_date")
    val updateDate: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "withdraw_date")
    val withdrawDate: LocalDateTime? = null,
    
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val socialAccounts: List<SocialAccount> = emptyList()
)

enum class UserState(val displayName: String) {
    ACTIVE("정상"),
    SUSPENDED("정지"),
    WITHDRAWN("탈퇴"),
    BANNED("차단")
}

enum class Gender(val displayName: String) {
    NONE("선택안함"),
    MALE("남"),
    FEMALE("여")
}

enum class ConsentType(val displayName: String) {
    AGREE("동의"),
    DISAGREE("비동의")
} 