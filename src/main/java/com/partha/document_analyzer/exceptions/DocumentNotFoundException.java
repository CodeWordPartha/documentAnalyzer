package com.partha.document_analyzer.exceptions;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(String message) {
        super(message);
    }

    public DocumentNotFoundException(long documentId) {
        super("Document not found with id:" + documentId);
    }
}
