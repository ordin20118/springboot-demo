package com.example.demo.controller

import com.example.demo.domain.UserState
import com.example.demo.domain.SocialProvider
import com.example.demo.dto.UserResponse
import com.example.demo.dto.UserUpdateRequest
import com.example.demo.dto.UserWithdrawRequest
import com.example.demo.dto.SocialAccountRequest
import com.example.demo.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/users")
@Tag(name = "사용자", description = "사용자 관리 API")
class UserController(
    private val userService: UserService
) {
    
    @GetMapping
    @Operation(summary = "전체 사용자 조회", description = "모든 사용자 목록을 조회합니다")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "사용자 조회", description = "특정 사용자 정보를 조회합니다")
    fun getUserById(
        @Parameter(description = "사용자 ID") @PathVariable id: Long
    ): ResponseEntity<UserResponse> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }
    
    @GetMapping("/state/{state}")
    @Operation(summary = "상태별 사용자 조회", description = "특정 상태를 가진 사용자들을 조회합니다")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUsersByState(
        @Parameter(description = "사용자 상태") @PathVariable state: UserState
    ): ResponseEntity<List<UserResponse>> {
        val users = userService.getUsersByState(state)
        return ResponseEntity.ok(users)
    }
    
    @GetMapping("/search")
    @Operation(summary = "이메일로 사용자 검색", description = "이메일에 특정 문자열이 포함된 사용자들을 검색합니다")
    @PreAuthorize("hasRole('ADMIN')")
    fun searchUsersByEmail(
        @Parameter(description = "검색할 이메일") @RequestParam email: String
    ): ResponseEntity<List<UserResponse>> {
        val users = userService.searchUsersByEmail(email)
        return ResponseEntity.ok(users)
    }
    
    @GetMapping("/search/complex")
    @Operation(summary = "복합 조건으로 사용자 검색", description = "닉네임과 상태를 조건으로 사용자들을 검색합니다")
    @PreAuthorize("hasRole('ADMIN')")
    fun searchUsersByComplexCondition(
        @Parameter(description = "검색할 닉네임") @RequestParam(required = false) nickname: String?,
        @Parameter(description = "검색할 상태") @RequestParam(required = false) state: UserState?
    ): ResponseEntity<List<UserResponse>> {
        val users = userService.searchUsersByComplexCondition(nickname, state)
        return ResponseEntity.ok(users)
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다")
    fun updateUser(
        @Parameter(description = "사용자 ID") @PathVariable id: Long,
        @Valid @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.updateUser(id, request)
        return ResponseEntity.ok(user)
    }
    
    @PostMapping("/{id}/withdraw")
    @Operation(summary = "회원 탈퇴", description = "사용자를 탈퇴 처리합니다")
    fun withdrawUser(
        @Parameter(description = "사용자 ID") @PathVariable id: Long,
        @Valid @RequestBody request: UserWithdrawRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.withdrawUser(id, request)
        return ResponseEntity.ok(user)
    }
    
    @PostMapping("/{id}/login")
    @Operation(summary = "로그인 시간 업데이트", description = "사용자의 마지막 로그인 시간을 업데이트합니다")
    fun updateLastLogin(
        @Parameter(description = "사용자 ID") @PathVariable id: Long
    ): ResponseEntity<Void> {
        userService.updateLastLogin(id)
        return ResponseEntity.ok().build()
    }
    
    @PostMapping("/{id}/social-accounts")
    @Operation(summary = "소셜 계정 연결", description = "사용자에게 소셜 계정을 연결합니다")
    fun connectSocialAccount(
        @Parameter(description = "사용자 ID") @PathVariable id: Long,
        @Valid @RequestBody request: SocialAccountRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.connectSocialAccount(id, request)
        return ResponseEntity.ok(user)
    }
    
    @DeleteMapping("/{id}/social-accounts/{provider}")
    @Operation(summary = "소셜 계정 연결 해제", description = "사용자의 소셜 계정 연결을 해제합니다")
    fun disconnectSocialAccount(
        @Parameter(description = "사용자 ID") @PathVariable id: Long,
        @Parameter(description = "소셜 플랫폼") @PathVariable provider: SocialProvider
    ): ResponseEntity<UserResponse> {
        val user = userService.disconnectSocialAccount(id, provider)
        return ResponseEntity.ok(user)
    }
} 