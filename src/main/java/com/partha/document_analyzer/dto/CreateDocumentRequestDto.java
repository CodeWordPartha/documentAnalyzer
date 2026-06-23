package com.partha.document_analyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateDocumentRequestDto {

    @NotBlank
    @Size(min = 3, max = 255, message = "title must be between 3 and 255 characters")
    private String title;

    @Size(max = 1000, message = "description must not exceed 1000 characters")
    private String description;

    private String content;
}
