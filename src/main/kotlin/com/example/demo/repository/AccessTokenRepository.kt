package com.example.demo.repository

import com.example.demo.domain.AccessToken
import com.example.demo.domain.TokenPlatform
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface AccessTokenRepository : JpaRepository<AccessToken, Long> {
    
    fun findByTokenHash(tokenHash: String): Optional<AccessToken>
    
    fun findByUserId(userId: Long): List<AccessToken>
    
    fun findByUserIdAndPlatform(userId: Long, platform: TokenPlatform): List<AccessToken>
    
    fun existsByTokenHash(tokenHash: String): Boolean
    
    @Query("SELECT a FROM AccessToken a WHERE a.userId = :userId AND a.revokedAt IS NULL AND a.expireDate > :now")
    fun findActiveTokensByUserId(@Param("userId") userId: Long, @Param("now") now: LocalDateTime): List<AccessToken>
    
    @Query("SELECT a FROM AccessToken a WHERE a.tokenHash = :tokenHash AND a.revokedAt IS NULL AND a.expireDate > :now")
    fun findActiveTokenByHash(@Param("tokenHash") tokenHash: String, @Param("now") now: LocalDateTime): Optional<AccessToken>
    
    @Modifying
    @Query("UPDATE AccessToken a SET a.revokedAt = :revokedAt WHERE a.userId = :userId AND a.revokedAt IS NULL")
    fun revokeAllTokensByUserId(@Param("userId") userId: Long, @Param("revokedAt") revokedAt: LocalDateTime): Int
    
    @Modifying
    @Query("UPDATE AccessToken a SET a.revokedAt = :revokedAt WHERE a.tokenHash = :tokenHash")
    fun revokeTokenByHash(@Param("tokenHash") tokenHash: String, @Param("revokedAt") revokedAt: LocalDateTime): Int
    
    @Modifying
    @Query("DELETE FROM AccessToken a WHERE a.expireDate < :expireDate")
    fun deleteExpiredTokens(@Param("expireDate") expireDate: LocalDateTime): Int
} 