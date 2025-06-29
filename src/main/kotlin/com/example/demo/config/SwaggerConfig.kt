package com.example.demo.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    
    @Bean
    fun openAPI(): OpenAPI {
        val info = Info()
            .title("Spring Boot Demo API")
            .description("Spring Boot + Kotlin + JWT + QueryDSL 데모 프로젝트")
            .version("1.0.0")
        
        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .name("JWT")
        
        val securityRequirement = SecurityRequirement().addList("JWT")
        
        return OpenAPI()
            .info(info)
            .components(Components().addSecuritySchemes("JWT", securityScheme))
            .addSecurityItem(securityRequirement)
    }
} 