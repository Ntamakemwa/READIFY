package com.readify.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends ReadifyException {
    
    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS");
    }
    
    public UnauthorizedAccessException() {
        super("Access denied", HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS");
    }
}
