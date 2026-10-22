package com.readify.common.exception;

import org.springframework.http.HttpStatus;

public class ReadifyException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public ReadifyException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    public ReadifyException(String message, HttpStatus status) {
        this(message, status, status.name());
    }

    public ReadifyException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public ReadifyException(String message, Throwable cause) {
        this(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    public ReadifyException(String message, Throwable cause, HttpStatus status) {
        this(message, cause, status, status.name());
    }

    public ReadifyException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
