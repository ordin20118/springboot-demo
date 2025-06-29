package com.example.demo.integration

import com.example.demo.domain.User
import com.example.demo.domain.UserRole
import com.example.demo.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDateTime

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {
    
    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    
    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper
    
    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        objectMapper = ObjectMapper()
        userRepository.deleteAll()
    }
    
    @Test
    fun `signup should create user successfully`() {
        // Given
        val signupRequest = mapOf(
            "email" to "test@example.com",
            "password" to "password123",
            "name" to "테스트 사용자"
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("테스트 사용자"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.password").doesNotExist())
    }
    
    @Test
    fun `signup should fail with invalid email`() {
        // Given
        val signupRequest = mapOf(
            "email" to "invalid-email",
            "password" to "password123",
            "name" to "테스트 사용자"
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `signup should fail with short password`() {
        // Given
        val signupRequest = mapOf(
            "email" to "test@example.com",
            "password" to "123",
            "name" to "테스트 사용자"
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isBadRequest)
    }
    
    @Test
    fun `login should return token for valid credentials`() {
        // Given
        val user = createTestUser("test@example.com", "password123", "테스트 사용자")
        userRepository.save(user)
        
        val loginRequest = mapOf(
            "email" to "test@example.com",
            "password" to "password123"
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
    }
    
    @Test
    fun `login should fail with invalid credentials`() {
        // Given
        val user = createTestUser("test@example.com", "password123", "테스트 사용자")
        userRepository.save(user)
        
        val loginRequest = mapOf(
            "email" to "test@example.com",
            "password" to "wrongpassword"
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
    }
    
    @Test
    fun `getUserById should return user when authenticated`() {
        // Given
        val user = createTestUser("test@example.com", "password123", "테스트 사용자")
        val savedUser = userRepository.save(user)
        
        val token = getAuthToken("test@example.com", "password123")
        
        // When & Then
        mockMvc.perform(
            get("/api/users/${savedUser.id}")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(savedUser.id))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("테스트 사용자"))
    }
    
    @Test
    fun `getUserById should fail without authentication`() {
        // Given
        val user = createTestUser("test@example.com", "password123", "테스트 사용자")
        val savedUser = userRepository.save(user)
        
        // When & Then
        mockMvc.perform(
            get("/api/users/${savedUser.id}")
        )
            .andExpect(status().isUnauthorized)
    }
    
    private fun createTestUser(email: String, password: String, name: String): User {
        return User(
            id = null,
            email = email,
            password = passwordEncoder.encode(password),
            name = name,
            role = UserRole.USER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    private fun getAuthToken(email: String, password: String): String {
        val loginRequest = mapOf(
            "email" to email,
            "password" to password
        )
        
        val response = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andReturn()
        
        val responseBody = response.response.contentAsString
        val tokenResponse = objectMapper.readValue(responseBody, Map::class.java)
        return tokenResponse["accessToken"] as String
    }
} 