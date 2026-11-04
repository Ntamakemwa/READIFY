package com.readify.service;

import com.readify.entity.User;
import com.readify.auth.repository.UserRepository;
import com.readify.common.exception.BusinessRuleException;
import com.readify.common.exception.ResourceNotFoundException;
import com.readify.oauth2.EmailVerificationService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class EmailVerificationManagementService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;

    public EmailVerificationManagementService(UserRepository userRepository,
                                              EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
    }

    public Map<String, Object> verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("User", "verification token", token));

        if (user.getEmailVerificationExpires() != null &&
                user.getEmailVerificationExpires().isBefore(Instant.now())) {
            user.setEmailVerificationToken(UUID.randomUUID().toString());
            user.setEmailVerificationExpires(Instant.now().plusSeconds(24 * 60 * 60));
            userRepository.save(user);
            emailVerificationService.sendVerificationEmail(user);
            throw new BusinessRuleException(
                    "Verification token expired. A new verification email has been sent to " + user.getEmail());
        }

        if (user.isEmailVerified()) {
            return Map.of(
                    "message", "Email is already verified",
                    "email", user.getEmail(),
                    "verified", true
            );
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpires(null);
        user.setEnabled(true);
        userRepository.save(user);

        return Map.of(
                "message", "Email verified successfully! Your account is now active.",
                "email", user.getEmail(),
                "verified", true,
                "enabled", true,
                "provider", user.getProvider().name()
        );
    }

    public Map<String, Object> resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isEmailVerified()) {
            throw new BusinessRuleException("Email is already verified");
        }

        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpires(Instant.now().plusSeconds(24 * 60 * 60));
        userRepository.save(user);
        emailVerificationService.sendVerificationEmail(user);

        return Map.of(
                "message", "Verification email sent successfully",
                "email", user.getEmail(),
                "expiresIn", "24 hours"
        );
    }

    public Map<String, Object> getVerificationStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return Map.of(
                "email", user.getEmail(),
                "verified", user.isEmailVerified(),
                "enabled", user.isEnabled(),
                "provider", user.getProvider().name(),
                "hasVerificationToken", user.getEmailVerificationToken() != null,
                "tokenExpires", user.getEmailVerificationExpires() != null
                        ? user.getEmailVerificationExpires().toString() : "none"
        );
    }
}
