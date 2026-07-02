package com.partha.document_analyzer.entities;

import com.partha.document_analyzer.enums.Sentiment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.w3c.dom.Text;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "document")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "Text")
    private String description;

    @Column(columnDefinition = "Text")
    private String content;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @ManyToOne(fetch = FetchType.LAZY) // Don't load user data unless explicitly requested.  Many documents belong to one user
    @JoinColumn(name = "user_id", nullable = false) // Foreign key column in documents table
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_encrypted")
    private Boolean isEncrypted;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @PrePersist
    protected void onCreated() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @Column(name = "ai_summary", columnDefinition = "Text")
    private String aiSummary;

    @Column(name = "ai_analyzed_at")
    private LocalDateTime aiAnalyzedAt;

    @Column(name = "ai_document_type", length = 100)
    private String aiDocumentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_sentiment", length = 50)
    private Sentiment aiSentiment;

    @ElementCollection
    @CollectionTable(name = "document_key_topics", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "topic")
    private List<String> aiKeyTopics;


    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
