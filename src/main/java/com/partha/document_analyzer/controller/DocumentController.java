package com.partha.document_analyzer.controller;

import com.partha.document_analyzer.dto.*;
import com.partha.document_analyzer.services.DocumentService;
import com.partha.document_analyzer.services.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<SuccessResponseDto> createDocument(@RequestParam Long userId, @Valid @RequestBody CreateDocumentRequestDto request) {

        DocumentResponseDto document = documentService.createDocument(userId, request);
        SuccessResponseDto successResponseDto = new SuccessResponseDto("Document created successfully", document);


        return new ResponseEntity<>(successResponseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailResponseDto> getById(@PathVariable Long id, @RequestParam Long userId) {

        DocumentDetailResponseDto document = documentService.getDocumentById(id, userId);

        return ResponseEntity.ok(document);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentResponseDto>> getUserDocuments(@PathVariable Long userId) {
        List<DocumentResponseDto> documents = documentService.getUserDocument(userId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<DocumentResponseDto>> getUserDocumentPaginated(
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DocumentResponseDto> documents = documentService.getUserDocumentPaginated(userId, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentResponseDto>> searchDocuments(
            @RequestParam Long userId,
            @RequestParam String keyword) {

        List<DocumentResponseDto> documents = documentService.searchDocuments(userId, keyword);
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponseDto> updateDocument(
            @PathVariable Long id,
            @RequestParam Long userId,
            @Valid @RequestBody UpdateDocumentRequestDto request) {

        DocumentResponseDto document = documentService.updateDocument(id, userId, request);

        SuccessResponseDto response = new SuccessResponseDto(
                "Document updated successfully",
                document
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponseDto> deleteDocument(
            @PathVariable Long id,
            @RequestParam Long userId) {

        documentService.deleteDocumentWithFile(id, userId);

        SuccessResponseDto response = new SuccessResponseDto(
                "Document deleted successfully"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<SuccessResponseDto> getDocumentCount(@PathVariable Long userId) {
        long count = documentService.countUserDocuments(userId);

        SuccessResponseDto response = new SuccessResponseDto(
                "Document count retrieved",
                count
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponseDto>> getAllDocuments() {
        List<DocumentResponseDto> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/upload")
    public ResponseEntity<SuccessResponseDto> uploadDocument(
            @RequestParam Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String content) {

        // Create DTO
        CreateDocumentRequestDto request = new CreateDocumentRequestDto();
        request.setTitle(title);
        request.setDescription(description);
        request.setContent(content);

        // Upload and create document
        DocumentResponseDto document = documentService.createDocumentWithFile(userId, request, file);

        SuccessResponseDto response = new SuccessResponseDto(
                "Document uploaded successfully",
                document
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Download document file
     * GET /api/documents/{id}/download?userId=1
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id,
            @RequestParam Long userId) {

        // Get file path (unique filename on server)
        String filePath = documentService.getFilePath(id, userId);
        String fileName = documentService.getFileName(id, userId);

        // Load physical file from disk
        Resource resource = fileStorageService.loadFileAsResource(filePath);

        // Get document metadata for content type
        DocumentDetailResponseDto document = documentService.getDocumentById(id, userId);
        String contentType = document.getFileType() != null
                ? document.getFileType()
                : "application/octet-stream";

        // Return file with original filename
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }
}
