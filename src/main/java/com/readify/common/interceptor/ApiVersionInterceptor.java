package com.readify.common.interceptor;

import com.readify.common.config.ApiVersion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Add API version headers
        response.setHeader(ApiVersion.VERSION_HEADER, ApiVersion.CURRENT_VERSION);
        
        // Handle deprecated versions
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/api/v2")) {
            response.setHeader(ApiVersion.DEPRECATED_HEADER, "true");
            response.setHeader(ApiVersion.SUNSET_HEADER, "2024-12-31");
        }
        
        return true;
    }
}
