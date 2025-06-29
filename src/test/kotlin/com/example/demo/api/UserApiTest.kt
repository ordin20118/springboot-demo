package com.example.demo.api

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
class UserApiTest {
    
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
    fun `GET /api/users should return 403 when not admin`() {
        // Given
        val user = createTestUser("user@example.com", "password123", "일반 사용자", UserRole.USER)
        userRepository.save(user)
        
        val token = getAuthToken("user@example.com", "password123")
        
        // When & Then
        mockMvc.perform(
            get("/api/users")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isForbidden)
    }
    
    @Test
    fun `GET /api/users should return all users when admin`() {
        // Given
        val admin = createTestUser("admin@example.com", "password123", "관리자", UserRole.ADMIN)
        val user1 = createTestUser("user1@example.com", "password123", "사용자1", UserRole.USER)
        val user2 = createTestUser("user2@example.com", "password123", "사용자2", UserRole.USER)
        
        userRepository.saveAll(listOf(admin, user1, user2))
        
        val token = getAuthToken("admin@example.com", "password123")
        
        // When & Then
        mockMvc.perform(
            get("/api/users")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].email").exists())
            .andExpect(jsonPath("$[0].name").exists())
            .andExpect(jsonPath("$[0].role").exists())
    }
    
    @Test
    fun `GET /api/users/{id} should return user when authenticated`() {
        // Given
        val user = createTestUser("test@example.com", "password123", "테스트 사용자", UserRole.USER)
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
    fun `GET /api/users/role/{role} should return users with specific role when admin`() {
        // Given
        val admin = createTestUser("admin@example.com", "password123", "관리자", UserRole.ADMIN)
        val user1 = createTestUser("user1@example.com", "password123", "사용자1", UserRole.USER)
        val user2 = createTestUser("user2@example.com", "password123", "사용자2", UserRole.USER)
        
        userRepository.saveAll(listOf(admin, user1, user2))
        
        val token = getAuthToken("admin@example.com", "password123")
        
        // When & Then
        mockMvc.perform(
            get("/api/users/role/USER")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].role").value("USER"))
            .andExpect(jsonPath("$[1].role").value("USER"))
    }
    
    @Test
    fun `GET /api/users/search should return users with matching email when admin`() {
        // Given
        val admin = createTestUser("admin@example.com", "password123", "관리자", UserRole.ADMIN)
        val user1 = createTestUser("test1@example.com", "password123", "테스트1", UserRole.USER)
        val user2 = createTestUser("other@example.com", "password123", "다른사용자", UserRole.USER)
        
        userRepository.saveAll(listOf(admin, user1, user2))
        
        val token = getAuthToken("admin@example.com", "password123")
        
        // When & Then
        mockMvc.perform(
            get("/api/users/search")
                .param("email", "test")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].email").value("test1@example.com"))
    }
    
    @Test
    fun `GET /api/users/search/complex should return users with complex conditions when admin`() {
        // Given
        val admin = createTestUser("admin@example.com", "password123", "관리자", UserRole.ADMIN)
        val user1 = createTestUser("user1@example.com", "password123", "김철수", UserRole.USER)
        val user2 = createTestUser("user2@example.com", "password123", "김영희", UserRole.USER)
        val user3 = createTestUser("user3@example.com", "password123", "박철수", UserRole.ADMIN)
        
        userRepository.saveAll(listOf(admin, user1, user2, user3))
        
        val token = getAuthToken("admin@example.com", "password123")
        
        // When & Then
        mockMvc.perform(
            get("/api/users/search/complex")
                .param("name", "김")
                .param("role", "USER")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].name").value("김철수"))
            .andExpect(jsonPath("$[1].name").value("김영희"))
    }
    
    @Test
    fun `POST /api/java/users/signup should create user successfully`() {
        // Given
        val signupRequest = mapOf(
            "email" to "java@example.com",
            "password" to "password123",
            "name" to "Java 사용자"
        )
        
        // When & Then
        mockMvc.perform(
            post("/api/java/users/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").value("java@example.com"))
            .andExpect(jsonPath("$.name").value("Java 사용자"))
    }
    
    private fun createTestUser(email: String, password: String, name: String, role: UserRole): User {
        return User(
            id = null,
            email = email,
            password = passwordEncoder.encode(password),
            name = name,
            role = role,
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