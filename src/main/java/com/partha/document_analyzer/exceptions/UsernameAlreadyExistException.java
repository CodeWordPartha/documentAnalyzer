package com.partha.document_analyzer.exceptions;

public class UsernameAlreadyExistException extends RuntimeException {

    public UsernameAlreadyExistException(String username) {

        super(username + "alreay exist ");
    }
}
