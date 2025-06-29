package com.example.demo.repository

import com.example.demo.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    
    fun findByEmail(email: String): Optional<User>
    
    fun existsByEmail(email: String): Boolean
    
    // Raw Query 예시
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    fun findByEmailRawQuery(@Param("email") email: String): Optional<User>
    
    @Query(value = "SELECT * FROM users WHERE role = :role", nativeQuery = true)
    fun findByRoleRawQuery(@Param("role") role: String): List<User>
    
    // JPQL 예시
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    fun findByNameContaining(@Param("name") name: String): List<User>
} 