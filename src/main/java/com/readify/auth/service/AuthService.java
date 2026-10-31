package com.readify.auth.service;

import com.readify.auth.dto.AuthResponse;
import com.readify.auth.dto.LoginRequest;
import com.readify.auth.dto.RegisterRequest;
import com.readify.auth.entity.AccountType;
import com.readify.auth.entity.User;
import com.readify.auth.entity.UserRole;
import com.readify.auth.repository.UserRepository;
import com.readify.common.exception.DuplicateResourceException;
import com.readify.common.exception.ResourceNotFoundException;
import com.readify.security.jwt.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final PasswordResetService passwordResetService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.passwordResetService = passwordResetService;
    }

    public AuthResponse login(@Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));

        String token = tokenProvider.generateToken(authentication);
        return new AuthResponse(token, user.getEmail(), user.getRole().name(), user.getAccountType().name());
    }

    public Map<String, Object> register(@Valid RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        AccountType accountType = request.getAccountType() == null
                ? AccountType.READER
                : request.getAccountType();

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // The account type never grants admin powers: everyone starts as a plain member.
        user.setRole(UserRole.ROLE_MEMBER);
        user.setAccountType(accountType);

        userRepository.save(user);
        return Map.of(
                "message", "Account registered successfully.",
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "accountType", user.getAccountType().name()
        );
    }

    public Map<String, Object> getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "status", user.getStatus().name(),
                "enabled", user.isEnabled(),
                "role", user.getRole().name(),
                "accountType", user.getAccountType().name()
        );
    }

    public Map<String, Object> forgotPassword(String email) {
        try {
            passwordResetService.createPasswordResetToken(email);
            return Map.of(
                    "message", "Password reset instructions sent to your email",
                    "email", email
            );
        } catch (ResourceNotFoundException e) {
            // Intentionally vague to prevent email enumeration
            return Map.of(
                    "message", "If an account with this email exists, password reset instructions have been sent"
            );
        }
    }

    public Map<String, Object> resetPassword(String resetToken, String newPassword) {
        passwordResetService.resetPassword(resetToken, newPassword);
        return Map.of("message", "Password reset successfully");
    }
}