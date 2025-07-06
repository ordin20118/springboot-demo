package com.example.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.NotBlank

@Configuration
@ConfigurationProperties(prefix = "apple.login")
@Validated
data class AppleLoginConfig(
    @field:NotBlank
    var teamId: String = "",
    
    @field:NotBlank
    var keyId: String = "",
    
    @field:NotBlank
    var bundleId: String = "",
    
    @field:NotBlank
    var privateKeyPath: String = "",
    
    var authKeysUrl: String = "https://appleid.apple.com/auth/keys",
    
    var issuer: String = "https://appleid.apple.com",
    
    var audience: String = "",
    
    var tokenExpirationTime: Long = 15777000 // 6개월 (초 단위)
) 