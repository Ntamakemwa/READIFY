package com.readify.auth.controller;

import com.readify.auth.service.EmailVerificationManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class EmailVerificationController {

    private final EmailVerificationManagementService emailVerificationManagementService;

    public EmailVerificationController(EmailVerificationManagementService emailVerificationManagementService) {
        this.emailVerificationManagementService = emailVerificationManagementService;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(emailVerificationManagementService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(@RequestParam String email) {
        return ResponseEntity.ok(emailVerificationManagementService.resendVerification(email));
    }

    @GetMapping("/verification-status")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(@RequestParam String email) {
        return ResponseEntity.ok(emailVerificationManagementService.getVerificationStatus(email));
    }
}
