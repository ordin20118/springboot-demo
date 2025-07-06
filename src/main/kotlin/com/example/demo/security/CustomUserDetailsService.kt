package com.example.demo.security

import com.example.demo.service.UserService
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userService: UserService
) : UserDetailsService {
    
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userService.getUserByEmail(email)
        
        return User.builder()
            .username(user.email)
            .password("") // 소셜 로그인 기반이므로 비밀번호 불필요
            .authorities(listOf(SimpleGrantedAuthority("ROLE_USER"))) // 기본 USER 권한
            .accountExpired(false)
            .accountLocked(user.state != com.example.demo.domain.UserState.ACTIVE)
            .credentialsExpired(false)
            .disabled(user.state == com.example.demo.domain.UserState.WITHDRAWN)
            .build()
    }
} 