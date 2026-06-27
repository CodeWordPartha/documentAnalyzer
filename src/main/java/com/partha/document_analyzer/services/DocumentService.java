package com.partha.document_analyzer.services;

import com.partha.document_analyzer.dto.CreateDocumentRequestDto;
import com.partha.document_analyzer.dto.DocumentDetailResponseDto;
import com.partha.document_analyzer.dto.DocumentResponseDto;
import com.partha.document_analyzer.dto.UpdateDocumentRequestDto;
import com.partha.document_analyzer.entities.Document;
import com.partha.document_analyzer.entities.User;
import com.partha.document_analyzer.exceptions.DocumentNotFoundException;
import com.partha.document_analyzer.exceptions.UserNotFoundException;
import com.partha.document_analyzer.repositories.DocumentRepository;
import com.partha.document_analyzer.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class DocumentService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final AiAnalysisService aiAnalysisService;
    private final TikaExtractionService tikaExtractionService;

    public DocumentResponseDto createDocument(Long userID, CreateDocumentRequestDto request) {

        User user = userRepository.findById(userID).orElseThrow(() -> new UserNotFoundException(userID));

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setContent(request.getContent());
        document.setUser(user);
        document.setIsEncrypted(false);
        document.setIsDeleted(false);

        documentRepository.save(document);

        return new DocumentResponseDto(document);
    }

    public DocumentDetailResponseDto getDocumentById(Long documentId,Long userId) {

        Document document = documentRepository.findByIdAndUserIdAndIsDeletedFalse(documentId, userId).orElseThrow(() -> new DocumentNotFoundException(documentId));

        return new DocumentDetailResponseDto(document);
    }

    public List<DocumentResponseDto> getUserDocument(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        List<Document> documentList = documentRepository.findByUserIdAndIsDeletedFalse(userId);
        return documentList.stream().map(doc -> new DocumentResponseDto(doc)).toList();
    }

    public Page<DocumentResponseDto> getUserDocumentPaginated(Long userId, Pageable pageable) {

        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        Page<Document> documents = documentRepository.findByUserIdAndIsDeletedFalse(userId, pageable);

        return documents.map(doc -> new DocumentResponseDto(doc));
    }

    public List<DocumentResponseDto> searchDocuments(Long userId, String keyword) {
        List<Document> documents = documentRepository
                .findByUserIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(userId, keyword);

        return documents.stream()
                .map(doc -> new DocumentResponseDto(doc))
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponseDto updateDocument(Long documentId, Long userId, UpdateDocumentRequestDto request) {

        Document document = documentRepository.findByIdAndUserIdAndIsDeletedFalse(documentId, userId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            document.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            document.setDescription(request.getDescription());
        }

        if (request.getContent() != null) {
            document.setContent(request.getContent());
        }

        Document updatedDocument = documentRepository.save(document);

        return new DocumentResponseDto(updatedDocument);
    }

    @Transactional
    public void deleteDocument(Long documentId, Long userId) {

        Document document = documentRepository.findByIdAndUserIdAndIsDeletedFalse(documentId, userId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        document.setIsDeleted(true);
        documentRepository.save(document);
    }

    public long countUserDocuments(Long userId) {
        return documentRepository.countByUserIdAndIsDeletedFalse(userId);
    }

    public List<DocumentResponseDto> getAllDocuments() {
        List<Document> documents = documentRepository.findByIsDeletedFalse();

        return documents.stream()
                .map(doc -> new DocumentResponseDto(doc))
                .collect(Collectors.toList());
    }

    public List<DocumentResponseDto> getDocumentsByFileType(String fileType) {
        List<Document> documents = documentRepository.findByFileTypeAndIsDeletedFalse(fileType);

        return documents.stream()
                .map(doc -> new DocumentResponseDto(doc))
                .collect(Collectors.toList());
    }

    public List<DocumentResponseDto> getLargeDocuments() {
        long oneMB = 1024 * 1024;
        List<Document> documents = documentRepository.findLargeDocuments(oneMB);

        return documents.stream()
                .map(doc -> new DocumentResponseDto(doc))
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponseDto createDocumentWithFile(
            Long userId,
            CreateDocumentRequestDto request,
            MultipartFile file) {

        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Store file
        String storedFilename = fileStorageService.storeFile(file);
        String originalFilename = file.getOriginalFilename();
        long fileSize = fileStorageService.getFileSize(file);
        String fileType = fileStorageService.getContentType(file);

        // extracting text from file
        String extractedContent = tikaExtractionService.extractText(file);

        // Create document
        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setContent(extractedContent);
        document.setFileName(originalFilename);      // Original name
        document.setFilePath(storedFilename);        // Unique name on server
        document.setFileSize(fileSize);
        document.setFileType(fileType);
        document.setUser(user);
        document.setIsEncrypted(false);
        document.setIsDeleted(false);

        // Save
        Document savedDocument = documentRepository.save(document);

        return new DocumentResponseDto(savedDocument);
    }

    @Transactional
    public void deleteDocumentWithFile(Long documentId, Long userId) {
        // Find document
        Document document = documentRepository.findByIdAndUserIdAndIsDeletedFalse(documentId, userId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        // Delete file from disk if exists
        if (document.getFilePath() != null) {
            fileStorageService.deleteFile(document.getFilePath());
        }

        // Soft delete document
        document.setIsDeleted(true);
        documentRepository.save(document);
    }

    public String getFilePath(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndUserIdAndIsDeletedFalse(documentId, userId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (document.getFilePath() == null) {
            throw new RuntimeException("Document has no file attached");
        }

        return document.getFilePath();
    }

    public String getFileName(Long documentId, Long userId) {
        Document document = documentRepository.findByIdAndUserIdAndIsDeletedFalse(documentId, userId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        return document.getFileName();
    }

    public DocumentDetailResponseDto analyzeDocument(Long documentId, Long userId) {

        Document document = documentRepository.findByIdAndUserIdAndIsDeletedFalse(documentId, userId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        String contentToAnalyze = document.getContent() != null
                ? document.getContent()
                : document.getDescription();
        String aiResult = aiAnalysisService.summarizeDocument(document.getTitle(), contentToAnalyze);
        document.setAiSummary(aiResult);
        document.setAiAnalyzedAt(LocalDateTime.now());
        documentRepository.save(document);

        return new DocumentDetailResponseDto(document);
    }

    public String answerQuestion(Long documentId, Long userId, String question) {

        Document document = documentRepository.findByIdAndUserIdAndIsDeletedFalse(documentId, userId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        String contentToUse = document.getContent() != null ? document.getContent() : document.getDescription();

        return aiAnalysisService.answerQuestion(document.getTitle(), contentToUse, question);
    }

}
