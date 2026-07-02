package com.partha.document_analyzer.dto;

import com.partha.document_analyzer.entities.Document;
import com.partha.document_analyzer.enums.Sentiment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailResponseDto {

    private Long id;
    private String title;
    private String description;
    private String content;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Boolean isEncrypted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long userId;
    private String username;
    private String aiSummary;
    private String aiDocumentType;
    private Sentiment aiSentiment;
    private List<String> aiKeyTopics;
    private LocalDateTime aiAnalyzedAt;

    public DocumentDetailResponseDto(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.description = document.getDescription();
        this.content = document.getContent();  // ← Full content
        this.fileName = document.getFileName();
        this.fileType = document.getFileType();
        this.fileSize = document.getFileSize();
        this.isEncrypted = document.getIsEncrypted();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        this.aiSummary = document.getAiSummary();
        this.aiDocumentType = document.getAiDocumentType();
        this.aiSentiment = document.getAiSentiment();
        this.aiKeyTopics = document.getAiKeyTopics();
        this.aiAnalyzedAt = document.getAiAnalyzedAt();

        if (document.getUser() != null) {
            this.userId = document.getUser().getId();
            this.username = document.getUser().getUsername();
        }
    }
}
