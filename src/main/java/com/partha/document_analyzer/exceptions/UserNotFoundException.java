package com.partha.document_analyzer.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId) {
        super("User not found with Id: " + userId);
    }

    public UserNotFoundException(String field, String value) {
        super("User not found with " + field + ":" + value );
    }
}
