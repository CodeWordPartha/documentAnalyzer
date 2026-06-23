package com.partha.document_analyzer.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super(email + "already registered");
    }
}
