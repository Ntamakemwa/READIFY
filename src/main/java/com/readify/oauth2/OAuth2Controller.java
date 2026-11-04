package com.readify.oauth2;

import com.readify.auth.dto.AuthResponse;
import com.readify.entity.User;
import com.readify.auth.repository.UserRepository;
import com.readify.auth.service.EmailVerificationManagementService;
import com.readify.common.exception.BusinessRuleException;
import com.readify.common.exception.ResourceNotFoundException;
import com.readify.security.jwt.JwtTokenProvider;
import com.readify.security.model.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth/oauth2")
public class OAuth2Controller {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2Controller.class);

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final EmailVerificationManagementService emailVerificationManagementService;

    public OAuth2Controller(UserRepository userRepository,
                            JwtTokenProvider tokenProvider,
                            EmailVerificationManagementService emailVerificationManagementService) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.emailVerificationManagementService = emailVerificationManagementService;
    }

    @GetMapping("/google/authorization-url")
    public ResponseEntity<Map<String, String>> getGoogleAuthorizationUrl() {
        return ResponseEntity.ok(Map.of(
                "authorizationUrl", "http://localhost:8081/oauth2/authorization/google",
                "provider", "google"
        ));
    }

    @PostMapping("/link-google")
    public ResponseEntity<?> linkGoogleAccount(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @RequestParam String linkingToken) {
        User user = userRepository.findById(Objects.requireNonNull(userPrincipal.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        if (user.isGoogleLinked()) {
            throw new BusinessRuleException("Google account is already linked");
        }

        return ResponseEntity.ok(Map.of(
                "message", "To link your Google account, please use the following authorization URL",
                "linkingUrl", "/api/v1/auth/oauth2/google/authorization-url?linking_token=" + linkingToken,
                "instructions", "Complete the Google authorization, then your accounts will be linked"
        ));
    }

    @PostMapping("/unlink-google")
    public ResponseEntity<?> unlinkGoogleAccount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(Objects.requireNonNull(userPrincipal.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        if (!user.isGoogleLinked()) {
            throw new BusinessRuleException("No Google account is linked");
        }

        user.setGoogleLinked(false);
        user.setGoogleId(null);
        user.setProvider(com.readify.entity.AuthProvider.LOCAL);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Google account unlinked successfully",
                "provider", "LOCAL"
        ));
    }

    @GetMapping("/success")
    public ResponseEntity<AuthResponse> oauth2Success(@AuthenticationPrincipal OAuth2User oauth2User) {
        try {
            String email = oauth2User.getAttribute("email");

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

            if (!user.isEmailVerified()) {
                return ResponseEntity.status(403).body(new AuthResponse(
                        null, email, user.getRole().name(), "GOOGLE", false, false, "Email verification required"
                ));
            }

            if (!user.isEnabled()) {
                return ResponseEntity.status(403).body(new AuthResponse(
                        null, email, user.getRole().name(), "GOOGLE", false, false, "Account is disabled"
                ));
            }

            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String token = tokenProvider.generateToken(
                    org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                            .authenticated(userPrincipal, null, userPrincipal.getAuthorities())
            );

            AuthResponse response = new AuthResponse(token, email, user.getRole().name(), "GOOGLE", false, true, "Authentication successful");
            response.setAvatarUrl(user.getAvatarUrl());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing OAuth2 success", e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(Objects.requireNonNull(userPrincipal.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        return ResponseEntity.ok(emailVerificationManagementService.resendVerification(user.getEmail()));
    }
}
