package com.partha.document_analyzer.services;

import com.partha.document_analyzer.dto.CreateDocumentRequestDto;
import com.partha.document_analyzer.dto.DocumentDetailResponseDto;
import com.partha.document_analyzer.dto.DocumentResponseDto;
import com.partha.document_analyzer.entities.Document;
import com.partha.document_analyzer.entities.User;
import com.partha.document_analyzer.exceptions.DocumentNotFoundException;
import com.partha.document_analyzer.repositories.DocumentRepository;
import com.partha.document_analyzer.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SearchIndexService searchIndexService;

    private User testUser;
    private Document testDocument;

    @BeforeEach
    void setup() {

        testUser = new User();
        testUser.setId(4L);
        testUser.setEmail("partha@email.com");
        testUser.setUsername("PARTHA");

        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setTitle("Test Document");
        testDocument.setDescription("Test Description");
        testDocument.setContent("Test content for analysis");
        testDocument.setIsDeleted(false);
        testDocument.setUser(testUser);
        testDocument.setCreatedAt(LocalDateTime.now());
        testDocument.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getDocumentByIdShouldReturnDocumentWhenDocumentExist() {

        // Arrange
        when(documentRepository.findByIdAndUserIdAndIsDeletedFalse(1L, 4L )).
                thenReturn(Optional.of(testDocument));

        // Act
        DocumentDetailResponseDto document = documentService.getDocumentById(1L, 4L);

        // Assert
        assertNotNull(document);
        assertEquals("Test Document", document.getTitle());
        assertEquals("Test Description", document.getDescription());
        assertEquals("Test content for analysis", document.getContent());

    }

    @Test
    void getDocumentByIdShouldThrowExceptionWhenDocumentDoesntExist() {

        // Arrange
        when(documentRepository.findByIdAndUserIdAndIsDeletedFalse(99L, 4L)).
                thenReturn(Optional.empty());


        //Assert AND Act
        assertThrows(DocumentNotFoundException.class, () -> documentService.getDocumentById(99L, 4L));
    }

    @Test
    void getuserDocumentShouldReturnListOfUserDocument() {

        // Arrange
        List<Document> testDocumentList = List.of(testDocument);
        when(documentRepository.findByUserIdAndIsDeletedFalse(4L)).thenReturn((testDocumentList));
        when(userRepository.existsById(4L)).thenReturn(true);

        // Act
        List<DocumentResponseDto> userDocument = documentService.getUserDocument(4L);

        // Assert
        assertNotNull(userDocument);
        assertEquals(1, userDocument.size());
        assertEquals("Test Document", userDocument.get(0).getTitle());
    }

    @Test
    void createDocumentShoulsSaveAndReturnDocument() {

        //Arrange
        CreateDocumentRequestDto requestDto = new CreateDocumentRequestDto();
        requestDto.setTitle("New Document");
        requestDto.setContent("New content");
        requestDto.setDescription("New Description");

        // Create what DB would return after saving
        Document savedDocument = new Document();
        savedDocument.setId(5L);
        savedDocument.setTitle("New Document"); // matches request
        savedDocument.setDescription("New Description"); // matches request
        savedDocument.setContent("New content"); // matches request
        savedDocument.setUser(testUser);
        savedDocument.setIsDeleted(false);
        savedDocument.setCreatedAt(LocalDateTime.now());
        savedDocument.setUpdatedAt(LocalDateTime.now());


        when(userRepository.findById(4L)).thenReturn(Optional.of(testUser));
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        DocumentResponseDto documentResult = documentService.createDocument(4L, requestDto);

        assertNotNull(documentResult);
        assertEquals("New Document", documentResult.getTitle());
        assertEquals("New Description", documentResult.getDescription());
        verify(documentRepository, times(1)).save(any(Document.class));


    }
}
