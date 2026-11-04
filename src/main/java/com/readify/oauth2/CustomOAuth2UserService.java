package com.readify.oauth2;

import com.readify.entity.AuthProvider;
import com.readify.entity.User;
import com.readify.entity.UserRole;
import com.readify.auth.repository.UserRepository;
import com.readify.common.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    
    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    
    public CustomOAuth2UserService(UserRepository userRepository, 
                                   EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
    }
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            logger.error("Error processing OAuth2 user", e);
            throw new OAuth2AuthenticationException("Failed to process OAuth2 user: " + e.getMessage());
        }
    }
    
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String googleId = oAuth2User.getAttribute("sub");
        
        logger.info("OAuth2 login attempt - Provider: {}, Email: {}, Google ID: {}", provider, email, googleId);
        
        if (email == null) {
            throw new BusinessRuleException("Email is required from OAuth2 provider");
        }
        
        return userRepository.findByEmail(email)
                .map(existingUser -> handleExistingUser(existingUser, provider, googleId, name, picture, oAuth2User.getAttributes()))
                .orElseGet(() -> createNewUser(email, name, picture, googleId, provider));
    }
    
    private OAuth2User handleExistingUser(User existingUser, String provider, String googleId, String name, String picture, Map<String, Object> attributes) {
        logger.info("Existing user found - Email: {}, Current Provider: {}, New Provider: {}", 
                   existingUser.getEmail(), existingUser.getProvider(), provider);
        
        if (existingUser.getProvider() == AuthProvider.LOCAL && !existingUser.isGoogleLinked()) {
            throw new BusinessRuleException.AccountExistsException(
                "An account with this email already exists. Please log in with your password and link your Google account, or use a different email address."
            );
        }
        
        if (existingUser.getProvider() == AuthProvider.GOOGLE && !googleId.equals(existingUser.getGoogleId())) {
            throw new BusinessRuleException("Google account conflict. Please contact support.");
        }
        
        if (name != null && !name.equals(existingUser.getFullName())) {
            existingUser.setFullName(name);
        }
        
        if (picture != null && !picture.equals(existingUser.getAvatarUrl())) {
            existingUser.setAvatarUrl(picture);
        }
        
        if (provider.equals("google")) {
            existingUser.setGoogleLinked(true);
            existingUser.setGoogleId(googleId);
            existingUser.setProvider(AuthProvider.GOOGLE);
        }
        
        userRepository.save(existingUser);
        logger.info("Updated existing user - Email: {}, Provider: {}", existingUser.getEmail(), existingUser.getProvider());
        
        return new CustomOAuth2User(existingUser, attributes);
    }
    
    private OAuth2User createNewUser(String email, String name, String picture, String googleId, String provider) {
        logger.info("Creating new user - Email: {}, Provider: {}", email, provider);
        
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name != null ? name : "Unknown User");
        newUser.setAvatarUrl(picture);
        newUser.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
        newUser.setRole(UserRole.ROLE_MEMBER);
        
        if (provider.equals("google")) {
            newUser.setGoogleId(googleId);
            newUser.setGoogleLinked(true);
            newUser.setEmailVerified(false);
        }
        
        String verificationToken = UUID.randomUUID().toString();
        newUser.setEmailVerificationToken(verificationToken);
        
        User savedUser = userRepository.save(newUser);
        
        try {
            emailVerificationService.sendVerificationEmail(savedUser);
            logger.info("Verification email sent to new user - Email: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}", email, e);
        }
        
        logger.info("New user created successfully - Email: {}, Provider: {}, ID: {}", 
                   savedUser.getEmail(), savedUser.getProvider(), savedUser.getId());
        
        return new CustomOAuth2User(savedUser, null);
    }
}
