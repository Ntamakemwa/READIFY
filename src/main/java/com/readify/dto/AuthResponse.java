package com.readify.dto;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private String email;
    private String role;
    private String accountType;
    private String provider;
    private boolean isNewUser;
    private boolean emailVerified;
    private String message;
    private String avatarUrl;

    public AuthResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.provider = "LOCAL";
        this.isNewUser = false;
        this.emailVerified = true;
        this.message = "Authentication successful";
    }

    public AuthResponse(String token, String email, String role, String accountType) {
        this(token, email, role);
        this.accountType = accountType;
    }

    public AuthResponse(String token, String email, String role, String provider,
                        boolean isNewUser, boolean emailVerified, String message) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.isNewUser = isNewUser;
        this.emailVerified = emailVerified;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    // For backward compatibility
    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }
}