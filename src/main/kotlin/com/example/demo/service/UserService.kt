package com.example.demo.service

import com.example.demo.domain.User
import com.example.demo.domain.UserRole
import com.example.demo.dto.UserResponse
import com.example.demo.dto.UserSignupRequest
import com.example.demo.repository.UserRepository
import com.example.demo.repository.UserRepositoryCustom
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val userRepositoryCustom: UserRepositoryCustom,
    private val passwordEncoder: PasswordEncoder
) {
    
    fun signup(request: UserSignupRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다")
        }
        
        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name
        )
        
        val savedUser = userRepository.save(user)
        return savedUser.toUserResponse()
    }
    
    @Transactional(readOnly = true)
    fun getUserById(id: Long): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }
        return user.toUserResponse()
    }
    
    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }
    }
    
    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toUserResponse() }
    }
    
    @Transactional(readOnly = true)
    fun getUsersByRole(role: UserRole): List<UserResponse> {
        return userRepositoryCustom.findUsersByRole(role).map { it.toUserResponse() }
    }
    
    @Transactional(readOnly = true)
    fun searchUsersByEmail(email: String): List<UserResponse> {
        return userRepositoryCustom.findUsersByEmailContaining(email).map { it.toUserResponse() }
    }
    
    @Transactional(readOnly = true)
    fun searchUsersByComplexCondition(name: String?, role: UserRole?): List<UserResponse> {
        return userRepositoryCustom.findUsersByComplexCondition(name, role).map { it.toUserResponse() }
    }
    
    private fun User.toUserResponse(): UserResponse {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return UserResponse(
            id = this.id!!,
            email = this.email,
            name = this.name,
            role = this.role,
            createdAt = this.createdAt.format(formatter),
            updatedAt = this.updatedAt.format(formatter)
        )
    }
} 