package com.partha.document_analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponseDto {

    private boolean success;
    private String message;
    private Object data;  // Flexible - can hold any data

    public SuccessResponseDto(String message) {
        this.success = true;
        this.message = message;
    }

    public SuccessResponseDto(String message, Object data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }
}