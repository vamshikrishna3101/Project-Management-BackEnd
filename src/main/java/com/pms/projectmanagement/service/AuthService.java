package com.pms.projectmanagement.service;

import com.pms.projectmanagement.dto.AuthResponse;
import com.pms.projectmanagement.dto.LoginRequest;
import com.pms.projectmanagement.dto.RegisterRequest;
import com.pms.projectmanagement.entity.User;
import com.pms.projectmanagement.exception.DuplicateResourceException;
import com.pms.projectmanagement.repository.UserRepository;
import com.pms.projectmanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository = null;
    private final PasswordEncoder passwordEncoder = null;
    private final AuthenticationManager authenticationManager = null;
    private final JwtUtil jwtUtil = new JwtUtil();

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : User.Role.MEMBER)
                .build();
        User saved = userRepository.save(user);
        return buildAuthResponse(saved, jwtUtil.generateToken(saved));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = (User) auth.getPrincipal();
        return buildAuthResponse(user, jwtUtil.generateToken(user));
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token).type("Bearer")
                .id(user.getId()).name(user.getName())
                .email(user.getEmail()).role(user.getRole())
                .build();
    }
}