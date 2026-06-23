package com.partha.document_analyzer.dto;

import com.partha.document_analyzer.entities.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDto {

    private Long id;
    private String title;
    private String description;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Boolean isEncrypted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long userId;
    private String username;

    public DocumentResponseDto(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.description = document.getDescription();
        this.fileName = document.getFileName();
        this.fileType = document.getFileType();
        this.fileSize = document.getFileSize();
        this.isEncrypted = document.getIsEncrypted();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();

        if (document.getUser() != null) {
            this.userId = document.getUser().getId();
            this.username = document.getUser().getUsername();
        }
    }

}
