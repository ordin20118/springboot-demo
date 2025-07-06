package com.example.demo.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
@ConditionalOnProperty(
    name = ["spring.cache.type"],
    havingValue = "simple",
    matchIfMissing = true
)
class CacheConfig 