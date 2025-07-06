package com.example.demo.repository

import com.example.demo.domain.SocialAccount
import com.example.demo.domain.SocialProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SocialAccountRepository : JpaRepository<SocialAccount, Long> {
    
    fun findByProviderAndSocialId(provider: SocialProvider, socialId: String): Optional<SocialAccount>
    
    fun findByUserId(userId: Long): List<SocialAccount>
    
    fun findByProvider(provider: SocialProvider): List<SocialAccount>
    
    fun existsByProviderAndSocialId(provider: SocialProvider, socialId: String): Boolean
    
    fun deleteByUserId(userId: Long)
} 