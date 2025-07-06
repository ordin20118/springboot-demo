package com.example.demo.service

import com.example.demo.domain.AccessToken
import com.example.demo.domain.TokenPlatform
import com.example.demo.repository.AccessTokenRepository
import com.example.demo.security.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class AccessTokenService(
    private val accessTokenRepository: AccessTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    fun generateAndSaveToken(
        userId: Long,
        platform: TokenPlatform,
        userAgent: String
    ): String {
        // JWT 토큰 생성
        val token = jwtTokenProvider.generateToken(userId.toString())
        
        // 토큰 해시 생성
        val tokenHash = generateTokenHash(token)
        
        // 만료 시간 계산 (JWT의 만료 시간과 동일하게)
        val expireDate = LocalDateTime.now().plusHours(24) // 24시간
        
        // AccessToken 엔티티 생성 및 저장
        val accessToken = AccessToken(
            userId = userId,
            tokenHash = tokenHash,
            token = token, // 실제 운영 환경에서는 암호화 고려
            platform = platform,
            userAgent = userAgent,
            expireDate = expireDate
        )
        
        accessTokenRepository.save(accessToken)
        
        return token
    }
    
    @Transactional(readOnly = true)
    fun validateToken(token: String): Boolean {
        val tokenHash = generateTokenHash(token)
        val accessToken = accessTokenRepository.findActiveTokenByHash(tokenHash, LocalDateTime.now())
        
        return accessToken.isPresent && jwtTokenProvider.validateToken(token)
    }
    
    @Transactional(readOnly = true)
    fun validateTokenByHash(tokenHash: String): Boolean {
        val accessToken = accessTokenRepository.findActiveTokenByHash(tokenHash, LocalDateTime.now())
        
        return if (accessToken.isPresent) {
            val token = accessToken.get().token
            token != null && jwtTokenProvider.validateToken(token)
        } else {
            false
        }
    }
    
    @Transactional(readOnly = true)
    fun getUserIdFromToken(token: String): Long? {
        val tokenHash = generateTokenHash(token)
        val accessToken = accessTokenRepository.findActiveTokenByHash(tokenHash, LocalDateTime.now())
        
        return if (accessToken.isPresent && jwtTokenProvider.validateToken(token)) {
            accessToken.get().userId
        } else {
            null
        }
    }
    
    @Transactional(readOnly = true)
    fun getUserIdFromTokenHash(tokenHash: String): Long? {
        val accessToken = accessTokenRepository.findActiveTokenByHash(tokenHash, LocalDateTime.now())
        
        return if (accessToken.isPresent) {
            val token = accessToken.get().token
            if (token != null && jwtTokenProvider.validateToken(token)) {
                accessToken.get().userId
            } else {
                null
            }
        } else {
            null
        }
    }
    
    fun revokeToken(token: String): Boolean {
        val tokenHash = generateTokenHash(token)
        val revokedCount = accessTokenRepository.revokeTokenByHash(tokenHash, LocalDateTime.now())
        return revokedCount > 0
    }
    
    fun revokeTokenByHash(tokenHash: String): Boolean {
        val revokedCount = accessTokenRepository.revokeTokenByHash(tokenHash, LocalDateTime.now())
        return revokedCount > 0
    }
    
    fun revokeAllUserTokens(userId: Long): Int {
        return accessTokenRepository.revokeAllTokensByUserId(userId, LocalDateTime.now())
    }
    
    @Transactional(readOnly = true)
    fun getUserActiveTokens(userId: Long): List<AccessToken> {
        return accessTokenRepository.findActiveTokensByUserId(userId, LocalDateTime.now())
    }
    
    fun cleanupExpiredTokens(): Int {
        return accessTokenRepository.deleteExpiredTokens(LocalDateTime.now())
    }
    
    private fun generateTokenHash(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
} 