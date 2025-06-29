package com.example.demo.controller

import com.example.demo.domain.UserRole
import com.example.demo.dto.UserResponse
import com.example.demo.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

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
    
    @GetMapping("/role/{role}")
    @Operation(summary = "역할별 사용자 조회", description = "특정 역할을 가진 사용자들을 조회합니다")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUsersByRole(
        @Parameter(description = "사용자 역할") @PathVariable role: UserRole
    ): ResponseEntity<List<UserResponse>> {
        val users = userService.getUsersByRole(role)
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
    @Operation(summary = "복합 조건으로 사용자 검색", description = "이름과 역할을 조건으로 사용자들을 검색합니다")
    @PreAuthorize("hasRole('ADMIN')")
    fun searchUsersByComplexCondition(
        @Parameter(description = "검색할 이름") @RequestParam(required = false) name: String?,
        @Parameter(description = "검색할 역할") @RequestParam(required = false) role: UserRole?
    ): ResponseEntity<List<UserResponse>> {
        val users = userService.searchUsersByComplexCondition(name, role)
        return ResponseEntity.ok(users)
    }
} 