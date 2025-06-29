package com.example.demo.java;

import com.example.demo.domain.User;
import com.example.demo.domain.UserRole;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserSignupRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserRepositoryCustom;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class JavaUserService {
    
    private final UserRepository userRepository;
    private final UserRepositoryCustom userRepositoryCustom;
    private final PasswordEncoder passwordEncoder;
    
    public JavaUserService(UserRepository userRepository, 
                          UserRepositoryCustom userRepositoryCustom, 
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRepositoryCustom = userRepositoryCustom;
        this.passwordEncoder = passwordEncoder;
    }
    
    public UserResponse signup(UserSignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }
        
        User user = new User(
            null,
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getName(),
            UserRole.USER,
            null,
            null
        );
        
        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        return toUserResponse(user);
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(this::toUserResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepositoryCustom.findUsersByRole(role)
            .stream()
            .map(this::toUserResponse)
            .collect(Collectors.toList());
    }
    
    private UserResponse toUserResponse(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole(),
            user.getCreatedAt().format(formatter),
            user.getUpdatedAt().format(formatter)
        );
    }
} 