package com.readify.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ReadifyException {
    
    public BusinessRuleException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION");
    }
    
    public BusinessRuleException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
    
    public static class LoanException extends BusinessRuleException {
        public LoanException(String message) {
            super(message, "LOAN_RULE_VIOLATION");
        }
    }
    
    public static class UserException extends BusinessRuleException {
        public UserException(String message) {
            super(message, "USER_RULE_VIOLATION");
        }
    }
    
    public static class BookException extends BusinessRuleException {
        public BookException(String message) {
            super(message, "BOOK_RULE_VIOLATION");
        }
    }
    
    public static class AccountExistsException extends BusinessRuleException {
        public AccountExistsException(String message) {
            super(message, "ACCOUNT_EXISTS");
        }
    }
}
