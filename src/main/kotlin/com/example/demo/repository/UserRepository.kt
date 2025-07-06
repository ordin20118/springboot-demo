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
    
    @Query(value = "SELECT * FROM users WHERE state = :state", nativeQuery = true)
    fun findByStateRawQuery(@Param("state") state: String): List<User>
    
    // JPQL 예시
    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:nickname%")
    fun findByNicknameContaining(@Param("nickname") nickname: String): List<User>
    
    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge")
    fun findByAgeBetween(@Param("minAge") minAge: Int, @Param("maxAge") maxAge: Int): List<User>
    
    @Query("SELECT u FROM User u WHERE u.state = :state")
    fun findByState(@Param("state") state: com.example.demo.domain.UserState): List<User>
} 