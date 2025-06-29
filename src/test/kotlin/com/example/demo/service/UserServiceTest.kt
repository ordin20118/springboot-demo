package com.example.demo.service

import com.example.demo.domain.User
import com.example.demo.domain.UserRole
import com.example.demo.dto.UserSignupRequest
import com.example.demo.repository.UserRepository
import com.example.demo.repository.UserRepositoryCustom
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.*

class UserServiceTest {
    
    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository
    private lateinit var userRepositoryCustom: UserRepositoryCustom
    private lateinit var passwordEncoder: PasswordEncoder
    
    @BeforeEach
    fun setUp() {
        userRepository = mock()
        userRepositoryCustom = mock()
        passwordEncoder = mock()
        userService = UserService(userRepository, userRepositoryCustom, passwordEncoder)
    }
    
    @Test
    fun `signup should create new user successfully`() {
        // Given
        val request = UserSignupRequest(
            email = "test@example.com",
            password = "password123",
            name = "테스트 사용자"
        )
        
        val encodedPassword = "encodedPassword123"
        val user = User(
            id = 1L,
            email = request.email,
            password = encodedPassword,
            name = request.name,
            role = UserRole.USER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)
        whenever(passwordEncoder.encode(request.password)).thenReturn(encodedPassword)
        whenever(userRepository.save(any())).thenReturn(user)
        
        // When
        val result = userService.signup(request)
        
        // Then
        assert(result.id == 1L)
        assert(result.email == request.email)
        assert(result.name == request.name)
        assert(result.role == UserRole.USER)
        
        verify(userRepository).existsByEmail(request.email)
        verify(passwordEncoder).encode(request.password)
        verify(userRepository).save(any())
    }
    
    @Test
    fun `signup should throw exception when email already exists`() {
        // Given
        val request = UserSignupRequest(
            email = "existing@example.com",
            password = "password123",
            name = "테스트 사용자"
        )
        
        whenever(userRepository.existsByEmail(request.email)).thenReturn(true)
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            userService.signup(request)
        }
        
        verify(userRepository).existsByEmail(request.email)
        verify(userRepository, never()).save(any())
    }
    
    @Test
    fun `getUserById should return user when exists`() {
        // Given
        val userId = 1L
        val user = User(
            id = userId,
            email = "test@example.com",
            password = "password123",
            name = "테스트 사용자",
            role = UserRole.USER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        
        // When
        val result = userService.getUserById(userId)
        
        // Then
        assert(result.id == userId)
        assert(result.email == user.email)
        assert(result.name == user.name)
        
        verify(userRepository).findById(userId)
    }
    
    @Test
    fun `getUserById should throw exception when user not found`() {
        // Given
        val userId = 999L
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            userService.getUserById(userId)
        }
        
        verify(userRepository).findById(userId)
    }
    
    @Test
    fun `getAllUsers should return all users`() {
        // Given
        val users = listOf(
            User(id = 1L, email = "user1@example.com", password = "pass1", name = "사용자1", role = UserRole.USER),
            User(id = 2L, email = "user2@example.com", password = "pass2", name = "사용자2", role = UserRole.ADMIN)
        )
        
        whenever(userRepository.findAll()).thenReturn(users)
        
        // When
        val result = userService.getAllUsers()
        
        // Then
        assert(result.size == 2)
        assert(result[0].id == 1L)
        assert(result[1].id == 2L)
        
        verify(userRepository).findAll()
    }
    
    @Test
    fun `getUsersByRole should return users with specific role`() {
        // Given
        val role = UserRole.ADMIN
        val users = listOf(
            User(id = 1L, email = "admin1@example.com", password = "pass1", name = "관리자1", role = UserRole.ADMIN),
            User(id = 2L, email = "admin2@example.com", password = "pass2", name = "관리자2", role = UserRole.ADMIN)
        )
        
        whenever(userRepositoryCustom.findUsersByRole(role)).thenReturn(users)
        
        // When
        val result = userService.getUsersByRole(role)
        
        // Then
        assert(result.size == 2)
        assert(result.all { it.role == UserRole.ADMIN })
        
        verify(userRepositoryCustom).findUsersByRole(role)
    }
} 