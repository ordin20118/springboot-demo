package com.example.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "kakao.login")
data class KakaoLoginConfig(
    var clientId: String = "",
    var clientSecret: String = "",
    var redirectUri: String = "",
    var apiUrl: String = "https://kapi.kakao.com"
) 