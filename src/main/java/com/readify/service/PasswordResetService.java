package com.readify.service;

import com.readify.entity.User;
import com.readify.auth.repository.UserRepository;
import com.readify.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${readify.password-reset.expiry-hours:1}")
    private int resetTokenExpiryHours;

    @Value("${readify.base-url:http://localhost:8081}")
    private String baseUrl;

    public PasswordResetService(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(Instant.now().plusSeconds(resetTokenExpiryHours * 3600L));
        userRepository.save(user);

        sendPasswordResetEmail(user, resetToken);
        return resetToken;
    }

    public void resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findByPasswordResetToken(resetToken)
                .orElseThrow(() -> new ResourceNotFoundException("Password reset token", "token", resetToken));

        if (user.getPasswordResetExpires().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        userRepository.save(user);
    }

    private void sendPasswordResetEmail(User user, String resetToken) {
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        String subject = "Reset Your Readify Password";
        String body = "Hello " + user.getFullName() + ",\n\n" +
                "You requested to reset your password for your Readify account.\n\n" +
                "Click the link below to reset your password:\n" +
                resetUrl + "\n\n" +
                "This link will expire in " + resetTokenExpiryHours + " hour(s).\n\n" +
                "If you didn't request this password reset, please ignore this email.\n\n" +
                "Best regards,\nThe Readify Team";
        emailService.sendEmail(user.getEmail(), subject, body);
    }
}
