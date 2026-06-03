package com.nipun.auth.service;

import com.nipun.auth.dto.AuthDto.*;
import com.nipun.auth.entity.User;
import com.nipun.auth.repository.UserRepository;
import com.nipun.shared.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(AuthRequest request) {
        log.info("Authenticating user: {}", request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new SecurityException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new SecurityException("Invalid username or password");
        }

        if (request.getTenantId() != null && !request.getTenantId().isBlank()) {
            if (!request.getTenantId().equals(user.getTenantId())) {
                throw new SecurityException("Access denied for specified tenant");
            }
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getTenantId(), user.getRole());
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .tenantId(user.getTenantId())
                .role(user.getRole())
                .build();
    }

    public User register(RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .tenantId(request.getTenantId())
                .schoolId(request.getSchoolId())
                .role(request.getRole())
                .build();

        return userRepository.save(user);
    }
}
