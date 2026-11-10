package com.readify.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.readify.dto.AuthResponse;
import com.readify.entity.AuthProvider;
import com.readify.entity.User;
import com.readify.entity.UserRole;
import com.readify.repository.UserRepository;
import com.readify.security.JwtTokenProvider;
import com.readify.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        logger.info("OAuth2 authentication successful for user: {}", email);
        
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            
            if (user == null) {
                logger.info("Creating new user from OAuth2 for email: {}", email);
                user = createNewOAuth2User(oAuth2User);
            }
            
            if (!user.isEmailVerified()) {
                logger.warn("OAuth2 user email not verified: {}", email);
                sendVerificationRequiredResponse(response, user);
                return;
            }
            
            if (!user.isEnabled()) {
                logger.warn("OAuth2 user account disabled: {}", email);
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                                "Account is disabled. Please verify your email first.");
                return;
            }
            
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String token = tokenProvider.generateToken(
                org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                    .authenticated(userPrincipal, null, userPrincipal.getAuthorities())
            );
            
            AuthResponse authResponse = new AuthResponse(
                token, email, user.getRole().name(), "GOOGLE",
                false, true, "Authentication successful"
            );
            authResponse.setAvatarUrl(user.getAvatarUrl());

            String redirectUrl = "http://localhost:8081/index.html?token=" + token
                    + "&email=" + java.net.URLEncoder.encode(email, "UTF-8")
                    + "&role=" + user.getRole().name()
                    + "&avatarUrl=" + java.net.URLEncoder.encode(user.getAvatarUrl() != null ? user.getAvatarUrl() : "", "UTF-8");
            response.sendRedirect(redirectUrl);


            logger.info("OAuth2 authentication completed successfully for: {}", email);
            
        } catch (Exception e) {
            logger.error("Error processing OAuth2 authentication success", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to generate authentication response");
        }
    }
    
    private void sendVerificationRequiredResponse(HttpServletResponse response, User user) throws IOException {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", "Email verification required");
        responseBody.put("message", "Please check your email and verify your account to complete registration");
        responseBody.put("email", user.getEmail());
        responseBody.put("emailVerified", false);
        responseBody.put("provider", user.getProvider().name());
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
    
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Authentication failed");
        errorResponse.put("message", message);
        errorResponse.put("status", status);
        
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
    
    private User createNewOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String googleId = oAuth2User.getAttribute("sub");
        
        User user = new User();
        user.setEmail(email);
        user.setFullName(name != null ? name : email);
        user.setPassword("OAUTH2_USER_NO_PASSWORD"); // OAuth2 users don't have passwords
        user.setAvatarUrl(picture);
        user.setGoogleId(googleId);
        user.setGoogleLinked(true);
        user.setProvider(AuthProvider.GOOGLE);
        user.setRole(UserRole.ROLE_MEMBER);
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        
        return userRepository.save(user);
    }
}
