package com.readify.oauth2;

import com.readify.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {
    
    private final User user;
    private final Map<String, Object> attributes;
    
    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return java.util.Collections.emptyList();
    }
    
    @Override
    public String getName() {
        return user.getEmail();
    }
    
    public User getUser() {
        return user;
    }
    
    public String getEmail() {
        return user.getEmail();
    }
    
    public String getFullName() {
        return user.getFullName();
    }
    
    public String getAvatarUrl() {
        return user.getAvatarUrl();
    }
    
    public boolean isEmailVerified() {
        return user.isEmailVerified();
    }
    
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
