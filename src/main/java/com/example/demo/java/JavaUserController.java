package com.example.demo.java;

import com.example.demo.domain.UserRole;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserSignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/java/users")
@Tag(name = "Java 사용자", description = "Java로 작성된 사용자 관리 API")
public class JavaUserController {
    
    private final JavaUserService javaUserService;
    
    public JavaUserController(JavaUserService javaUserService) {
        this.javaUserService = javaUserService;
    }
    
    @PostMapping("/signup")
    @Operation(summary = "Java 회원가입", description = "Java로 작성된 회원가입 API")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        UserResponse userResponse = javaUserService.signup(request);
        return ResponseEntity.ok(userResponse);
    }
    
    @GetMapping
    @Operation(summary = "Java 전체 사용자 조회", description = "Java로 작성된 전체 사용자 조회 API")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = javaUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Java 사용자 조회", description = "Java로 작성된 특정 사용자 조회 API")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "사용자 ID") @PathVariable Long id) {
        UserResponse user = javaUserService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/role/{role}")
    @Operation(summary = "Java 역할별 사용자 조회", description = "Java로 작성된 역할별 사용자 조회 API")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(
            @Parameter(description = "사용자 역할") @PathVariable UserRole role) {
        List<UserResponse> users = javaUserService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
} 