package com.example.demo.security

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.util.ReflectionTestUtils
import java.util.*

class JwtTokenProviderTest {
    
    private lateinit var jwtTokenProvider: JwtTokenProvider
    
    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider()
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "test-secret-key-for-jwt-token-provider-test")
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L) // 1 hour
    }
    
    @Test
    fun `generateToken should create valid token`() {
        // Given
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = TestingAuthenticationToken("test@example.com", "password", authorities)
        authentication.isAuthenticated = true
        
        // When
        val token = jwtTokenProvider.generateToken(authentication)
        
        // Then
        assert(token.isNotEmpty())
        assert(jwtTokenProvider.validateToken(token))
    }
    
    @Test
    fun `getUsernameFromToken should extract username correctly`() {
        // Given
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = TestingAuthenticationToken("test@example.com", "password", authorities)
        authentication.isAuthenticated = true
        
        val token = jwtTokenProvider.generateToken(authentication)
        
        // When
        val username = jwtTokenProvider.getUsernameFromToken(token)
        
        // Then
        assert(username == "test@example.com")
    }
    
    @Test
    fun `validateToken should return true for valid token`() {
        // Given
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = TestingAuthenticationToken("test@example.com", "password", authorities)
        authentication.isAuthenticated = true
        
        val token = jwtTokenProvider.generateToken(authentication)
        
        // When
        val isValid = jwtTokenProvider.validateToken(token)
        
        // Then
        assert(isValid)
    }
    
    @Test
    fun `validateToken should return false for invalid token`() {
        // Given
        val invalidToken = "invalid.token.here"
        
        // When
        val isValid = jwtTokenProvider.validateToken(invalidToken)
        
        // Then
        assert(!isValid)
    }
    
    @Test
    fun `validateToken should return false for empty token`() {
        // Given
        val emptyToken = ""
        
        // When
        val isValid = jwtTokenProvider.validateToken(emptyToken)
        
        // Then
        assert(!isValid)
    }
    
    @Test
    fun `getUsernameFromToken should throw exception for invalid token`() {
        // Given
        val invalidToken = "invalid.token.here"
        
        // When & Then
        assertThrows<Exception> {
            jwtTokenProvider.getUsernameFromToken(invalidToken)
        }
    }
} 