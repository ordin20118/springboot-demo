package com.example.demo.java;

import com.example.demo.domain.User;
import com.example.demo.domain.UserRole;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserSignupRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRepositoryCustom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JavaUserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserRepositoryCustom userRepositoryCustom;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    private JavaUserService javaUserService;
    
    @BeforeEach
    void setUp() {
        javaUserService = new JavaUserService(userRepository, userRepositoryCustom, passwordEncoder);
    }
    
    @Test
    void signup_ShouldCreateNewUserSuccessfully() {
        // Given
        UserSignupRequest request = new UserSignupRequest(
            "test@example.com",
            "password123",
            "테스트 사용자"
        );
        
        String encodedPassword = "encodedPassword123";
        User user = new User(
            1L,
            request.getEmail(),
            encodedPassword,
            request.getName(),
            UserRole.USER,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        UserResponse result = javaUserService.signup(request);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo(request.getEmail());
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        
        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void signup_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        UserSignupRequest request = new UserSignupRequest(
            "existing@example.com",
            "password123",
            "테스트 사용자"
        );
        
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            javaUserService.signup(request);
        });
        
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        // Given
        Long userId = 1L;
        User user = new User(
            userId,
            "test@example.com",
            "password123",
            "테스트 사용자",
            UserRole.USER,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // When
        UserResponse result = javaUserService.getUserById(userId);
        
        // Then
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
        assertThat(result.getName()).isEqualTo(user.getName());
        
        verify(userRepository).findById(userId);
    }
    
    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            javaUserService.getUserById(userId);
        });
        
        verify(userRepository).findById(userId);
    }
    
    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        List<User> users = Arrays.asList(
            new User(1L, "user1@example.com", "pass1", "사용자1", UserRole.USER, LocalDateTime.now(), LocalDateTime.now()),
            new User(2L, "user2@example.com", "pass2", "사용자2", UserRole.ADMIN, LocalDateTime.now(), LocalDateTime.now())
        );
        
        when(userRepository.findAll()).thenReturn(users);
        
        // When
        List<UserResponse> result = javaUserService.getAllUsers();
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        
        verify(userRepository).findAll();
    }
    
    @Test
    void getUsersByRole_ShouldReturnUsersWithSpecificRole() {
        // Given
        UserRole role = UserRole.ADMIN;
        List<User> users = Arrays.asList(
            new User(1L, "admin1@example.com", "pass1", "관리자1", UserRole.ADMIN, LocalDateTime.now(), LocalDateTime.now()),
            new User(2L, "admin2@example.com", "pass2", "관리자2", UserRole.ADMIN, LocalDateTime.now(), LocalDateTime.now())
        );
        
        when(userRepositoryCustom.findUsersByRole(role)).thenReturn(users);
        
        // When
        List<UserResponse> result = javaUserService.getUsersByRole(role);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(userResponse -> userResponse.getRole() == UserRole.ADMIN);
        
        verify(userRepositoryCustom).findUsersByRole(role);
    }
} 