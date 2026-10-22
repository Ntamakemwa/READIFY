package com.readify.oauth2;

import com.readify.auth.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    
    private final JavaMailSender mailSender;

    @Value("${readify.oauth2.email-verification.base-url:http://localhost:8081}")
    private String baseUrl;

    public EmailVerificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendVerificationEmail(User user) {
        try {
            String verificationUrl = baseUrl + "/api/v1/auth/verify-email?token=" + user.getEmailVerificationToken();
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Verify your Readify account");
            message.setText(buildVerificationEmail(user, verificationUrl));
            
            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    private String buildVerificationEmail(User user, String verificationUrl) {
        return String.format(
            "Welcome to Readify!\n\n" +
            "Thank you for signing up with Google. To complete your registration, please verify your email address by clicking the link below:\n\n" +
            "%s\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you didn't create an account with Readify, please ignore this email.\n\n" +
            "Happy reading!\n" +
            "The Readify Team",
            verificationUrl
        );
    }
    
    public void sendAccountExistsNotification(User user, String provider) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Account Already Exists");
            message.setText(buildAccountExistsEmail(user, provider));
            
            mailSender.send(message);
            logger.info("Account exists notification sent to: {}", user.getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send account exists notification to: {}", user.getEmail(), e);
        }
    }
    
    private String buildAccountExistsEmail(User user, String provider) {
        return String.format(
            "Hello %s,\n\n" +
            "We noticed you tried to sign in with %s using an email that's already registered with Readify.\n\n" +
            "If this is your account, please:\n" +
            "1. Log in with your existing password\n" +
            "2. Go to account settings to link your %s account\n\n" +
            "Or, if you want to use %s exclusively, please contact our support team.\n\n" +
            "Thank you!\n" +
            "The Readify Team",
            user.getFullName(),
            provider.toUpperCase(),
            provider.toUpperCase(),
            provider.toUpperCase()
        );
    }
}
