package com.example.demo.security

import com.example.demo.service.AccessTokenService
import com.example.demo.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val accessTokenService: AccessTokenService,
    private val userService: UserService
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val tokenHash = getTokenHashFromRequest(request)
            
            if (!tokenHash.isNullOrBlank() && accessTokenService.validateTokenByHash(tokenHash)) {
                val userId = accessTokenService.getUserIdFromTokenHash(tokenHash)
                
                if (userId != null) {
                    val user = userService.getUserById(userId)
                    
                    // 사용자 상태 확인
                    if (user.state == com.example.demo.domain.UserState.ACTIVE) {
                        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                        val authentication = UsernamePasswordAuthenticationToken(
                            user.email, null, authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        
                        SecurityContextHolder.getContext().authentication = authentication
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Could not set user authentication in security context", ex)
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun getTokenHashFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
} 