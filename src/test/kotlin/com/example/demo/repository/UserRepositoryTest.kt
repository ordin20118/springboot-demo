package com.example.demo.repository

import com.example.demo.domain.User
import com.example.demo.domain.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

import org.assertj.core.api.Assertions.assertThat

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest {
    
    @Autowired
    private lateinit var userRepository: UserRepository
    
    private lateinit var testUser: User
    
    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        
        testUser = User(
            id = null,
            email = "test@example.com",
            password = "encodedPassword123",
            name = "테스트 사용자",
            role = UserRole.USER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    @Test
    fun `save should persist user successfully`() {
        // When
        val savedUser = userRepository.save(testUser)
        
        // Then
        assertThat(savedUser.id).isNotNull()
        assertThat(savedUser.email).isEqualTo("test@example.com")
        assertThat(savedUser.name).isEqualTo("테스트 사용자")
        assertThat(savedUser.role).isEqualTo(UserRole.USER)
    }
    
    @Test
    fun `findByEmail should return user when exists`() {
        // Given
        val savedUser = userRepository.save(testUser)
        
        // When
        val foundUser = userRepository.findByEmail("test@example.com")
        
        // Then
        assertThat(foundUser).isPresent
        assertThat(foundUser.get().id).isEqualTo(savedUser.id)
        assertThat(foundUser.get().email).isEqualTo("test@example.com")
    }
    
    @Test
    fun `findByEmail should return empty when user not exists`() {
        // When
        val foundUser = userRepository.findByEmail("nonexistent@example.com")
        
        // Then
        assertThat(foundUser).isEmpty
    }
    
    @Test
    fun `existsByEmail should return true when user exists`() {
        // Given
        userRepository.save(testUser)
        
        // When
        val exists = userRepository.existsByEmail("test@example.com")
        
        // Then
        assertThat(exists).isTrue()
    }
    
    @Test
    fun `existsByEmail should return false when user not exists`() {
        // When
        val exists = userRepository.existsByEmail("nonexistent@example.com")
        
        // Then
        assertThat(exists).isFalse()
    }
    
    @Test
    fun `findByEmailRawQuery should return user when exists`() {
        // Given
        val savedUser = userRepository.save(testUser)
        
        // When
        val foundUser = userRepository.findByEmailRawQuery("test@example.com")
        
        // Then
        assertThat(foundUser).isPresent
        assertThat(foundUser.get().id).isEqualTo(savedUser.id)
        assertThat(foundUser.get().email).isEqualTo("test@example.com")
    }
    
    @Test
    fun `findByRoleRawQuery should return users with specific role`() {
        // Given
        val user1 = testUser.copy(id = null, email = "user1@example.com", role = UserRole.USER)
        val user2 = testUser.copy(id = null, email = "admin1@example.com", role = UserRole.ADMIN)
        val user3 = testUser.copy(id = null, email = "admin2@example.com", role = UserRole.ADMIN)
        
        userRepository.saveAll(listOf(user1, user2, user3))
        
        // When
        val adminUsers = userRepository.findByRoleRawQuery("ADMIN")
        
        // Then
        assertThat(adminUsers).hasSize(2)
        assertThat(adminUsers).allMatch { it.role == UserRole.ADMIN }
    }
    
    @Test
    fun `findByNameContaining should return users with matching name`() {
        // Given
        val user1 = testUser.copy(id = null, email = "user1@example.com", name = "김철수")
        val user2 = testUser.copy(id = null, email = "user2@example.com", name = "김영희")
        val user3 = testUser.copy(id = null, email = "user3@example.com", name = "박철수")
        
        userRepository.saveAll(listOf(user1, user2, user3))
        
        // When
        val kimUsers = userRepository.findByNameContaining("김")
        
        // Then
        assertThat(kimUsers).hasSize(2)
        assertThat(kimUsers).allMatch { it.name.contains("김") }
    }
    
    @Test
    fun `findAll should return all users`() {
        // Given
        val user1 = testUser.copy(id = null, email = "user1@example.com")
        val user2 = testUser.copy(id = null, email = "user2@example.com")
        val user3 = testUser.copy(id = null, email = "user3@example.com")
        
        userRepository.saveAll(listOf(user1, user2, user3))
        
        // When
        val allUsers = userRepository.findAll()
        
        // Then
        assertThat(allUsers).hasSize(3)
    }
} 