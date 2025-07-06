package com.example.demo.service

import com.example.demo.domain.User
import com.example.demo.domain.UserState
import com.example.demo.domain.SocialAccount
import com.example.demo.domain.SocialProvider
import com.example.demo.dto.UserResponse
import com.example.demo.dto.SocialAccountResponse
import com.example.demo.dto.UserSignupRequest
import com.example.demo.dto.UserUpdateRequest
import com.example.demo.dto.UserWithdrawRequest
import com.example.demo.repository.UserRepository
import com.example.demo.repository.UserRepositoryCustom
import com.example.demo.repository.SocialAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val userRepositoryCustom: UserRepositoryCustom,
    private val socialAccountRepository: SocialAccountRepository
) {
    
    fun signup(request: UserSignupRequest): UserResponse {
        // 일반 회원가입의 경우만 이메일 중복 체크 (소셜 로그인은 같은 이메일 허용)
        if (request.socialAccount == null && userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다")
        }
        
        // 소셜 로그인 중복 체크 (소셜 계정이 있는 경우만)
        if (request.socialAccount != null) {
            if (socialAccountRepository.existsByProviderAndSocialId(
                    request.socialAccount.provider, 
                    request.socialAccount.socialId
                )) {
                throw IllegalArgumentException("이미 연결된 소셜 계정입니다")
            }
        }
        
        val user = User(
            email = request.email,
            nickname = request.nickname,
            profile = request.profile,
            age = request.age,
            gender = request.gender,
            marketing = request.marketing,
            termsOfService = request.termsOfService,
            personalInfoPolicy = request.personalInfoPolicy
        )
        
        val savedUser = userRepository.save(user)
        
        // 소셜 계정 연결
        if (request.socialAccount != null) {
            val socialAccount = SocialAccount(
                user = savedUser,
                provider = request.socialAccount.provider,
                socialId = request.socialAccount.socialId,
                email = request.socialAccount.email,
                accessToken = request.socialAccount.accessToken,
                refreshToken = request.socialAccount.refreshToken
            )
            socialAccountRepository.save(socialAccount)
        }
        
        return getUserById(savedUser.id!!)
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
    fun getUserBySocialId(provider: SocialProvider, socialId: String): User {
        val socialAccount = socialAccountRepository.findByProviderAndSocialId(provider, socialId)
            .orElseThrow { IllegalArgumentException("소셜 로그인 사용자를 찾을 수 없습니다") }
        return socialAccount.user
    }
    
    @Transactional(readOnly = true)
    fun existsBySocialId(provider: SocialProvider, socialId: String): Boolean {
        return socialAccountRepository.existsByProviderAndSocialId(provider, socialId)
    }
    
    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserResponse> {
        return userRepository.findAll().map { it.toUserResponse() }
    }
    
    @Transactional(readOnly = true)
    fun getUsersByState(state: UserState): List<UserResponse> {
        return userRepositoryCustom.findUsersByState(state).map { it.toUserResponse() }
    }
    
    @Transactional(readOnly = true)
    fun searchUsersByEmail(email: String): List<UserResponse> {
        return userRepositoryCustom.findUsersByEmailContaining(email).map { it.toUserResponse() }
    }
    
    @Transactional(readOnly = true)
    fun searchUsersByComplexCondition(nickname: String?, state: UserState?): List<UserResponse> {
        return userRepositoryCustom.findUsersByComplexCondition(nickname, state).map { it.toUserResponse() }
    }
    
    fun updateUser(id: Long, request: UserUpdateRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }
        
        val updatedUser = user.copy(
            nickname = request.nickname ?: user.nickname,
            age = request.age ?: user.age,
            gender = request.gender ?: user.gender,
            profile = request.profile ?: user.profile,
            marketing = request.marketing ?: user.marketing,
            updateDate = LocalDateTime.now()
        )
        
        val savedUser = userRepository.save(updatedUser)
        return savedUser.toUserResponse()
    }
    
    fun withdrawUser(id: Long, request: UserWithdrawRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }
        
        val withdrawnUser = user.copy(
            state = UserState.WITHDRAWN,
            withdrawalReason = request.withdrawalReason,
            withdrawDate = LocalDateTime.now(),
            updateDate = LocalDateTime.now()
        )
        
        val savedUser = userRepository.save(withdrawnUser)
        return savedUser.toUserResponse()
    }
    
    fun updateLastLogin(id: Long) {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }
        
        val updatedUser = user.copy(
            lastLogin = LocalDateTime.now(),
            updateDate = LocalDateTime.now()
        )
        
        userRepository.save(updatedUser)
    }
    
    fun connectSocialAccount(id: Long, request: com.example.demo.dto.SocialAccountRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }
        
        if (socialAccountRepository.existsByProviderAndSocialId(request.provider, request.socialId)) {
            throw IllegalArgumentException("이미 연결된 소셜 계정입니다")
        }
        
        val socialAccount = SocialAccount(
            user = user,
            provider = request.provider,
            socialId = request.socialId,
            email = request.email,
            accessToken = request.accessToken,
            refreshToken = request.refreshToken
        )
        
        socialAccountRepository.save(socialAccount)
        return getUserById(id)
    }
    
    fun disconnectSocialAccount(id: Long, provider: SocialProvider): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }
        
        val socialAccounts = socialAccountRepository.findByUserId(id)
        val targetAccount = socialAccounts.find { it.provider == provider }
            ?: throw IllegalArgumentException("연결된 소셜 계정을 찾을 수 없습니다")
        
        socialAccountRepository.delete(targetAccount)
        return getUserById(id)
    }
    
    private fun User.toUserResponse(): UserResponse {
        val socialAccounts = socialAccountRepository.findByUserId(this.id!!)
            .map { it.toSocialAccountResponse() }
        
        return UserResponse(
            id = this.id!!,
            email = this.email,
            nickname = this.nickname,
            profile = this.profile,
            age = this.age,
            gender = this.gender,
            state = this.state,
            lastLogin = this.lastLogin,
            marketing = this.marketing,
            termsOfService = this.termsOfService,
            personalInfoPolicy = this.personalInfoPolicy,
            regDate = this.regDate,
            updateDate = this.updateDate,
            socialAccounts = socialAccounts
        )
    }
    
    private fun SocialAccount.toSocialAccountResponse(): SocialAccountResponse {
        return SocialAccountResponse(
            id = this.id!!,
            provider = this.provider,
            socialId = this.socialId,
            email = this.email,
            connectedAt = this.connectedAt
        )
    }
} 