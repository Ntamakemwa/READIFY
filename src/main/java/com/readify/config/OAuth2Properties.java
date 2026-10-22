package com.readify.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "readify.oauth2")
public class OAuth2Properties {
    
    private String redirectUri;
    private EmailVerification emailVerification = new EmailVerification();
    
    public static class EmailVerification {
        private int expiryHours = 24;
        private String baseUrl = "http://localhost:8081";
        
        public int getExpiryHours() {
            return expiryHours;
        }
        
        public void setExpiryHours(int expiryHours) {
            this.expiryHours = expiryHours;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
    
    public String getRedirectUri() {
        return redirectUri;
    }
    
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    public EmailVerification getEmailVerification() {
        return emailVerification;
    }
    
    public void setEmailVerification(EmailVerification emailVerification) {
        this.emailVerification = emailVerification;
    }
}
