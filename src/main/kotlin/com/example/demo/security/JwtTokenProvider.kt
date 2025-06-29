package com.example.demo.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtTokenProvider {
    
    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String
    
    @Value("\${jwt.expiration}")
    private var jwtExpirationMs: Long = 0
    
    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetails
        
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)
        
        return Jwts.builder()
            .setSubject(userPrincipal.username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(Keys.hmacShaKeyFor(jwtSecret.toByteArray()), SignatureAlgorithm.HS512)
            .compact()
    }
    
    fun getUsernameFromToken(token: String): String {
        val claims = Jwts.parser()
            .setSigningKey(jwtSecret.toByteArray())
            .parseClaimsJws(token)
            .body
        return claims.subject
    }
    
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .setSigningKey(jwtSecret.toByteArray())
                .parseClaimsJws(token)
            true
        } catch (ex: Exception) {
            false
        }
    }
} 