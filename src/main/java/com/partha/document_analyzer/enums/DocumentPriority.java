package com.partha.document_analyzer.enums;

public enum DocumentPriority {

    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4);

    private final int value;

    DocumentPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
