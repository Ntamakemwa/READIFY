package com.readify.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.readify.common.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        logger.error("OAuth2 authentication failed: {}", exception.getMessage(), exception);
        
        String errorMessage = "OAuth2 authentication failed";
        int statusCode = HttpServletResponse.SC_UNAUTHORIZED;
        
        // Handle specific business rule exceptions
        if (exception.getCause() instanceof BusinessRuleException.AccountExistsException) {
            errorMessage = exception.getCause().getMessage();
            statusCode = HttpServletResponse.SC_CONFLICT;
        } else if (exception.getCause() instanceof BusinessRuleException) {
            errorMessage = exception.getCause().getMessage();
            statusCode = HttpServletResponse.SC_BAD_REQUEST;
        }
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "OAuth2 Authentication Failed");
        errorResponse.put("message", errorMessage);
        errorResponse.put("status", statusCode);
        errorResponse.put("timestamp", java.time.Instant.now().toString());
        
        // Add specific error details if available
        if (exception.getMessage() != null) {
            errorResponse.put("details", exception.getMessage());
        }
        
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
