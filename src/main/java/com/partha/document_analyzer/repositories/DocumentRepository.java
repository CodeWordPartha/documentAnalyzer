package com.partha.document_analyzer.repositories;

import com.partha.document_analyzer.entities.Document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUserIdAndIsDeletedFalse(Long userId);

    Page<Document> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<Document> findByIdAndUserIdAndIsDeletedFalse(Long id, Long userId);

    List<Document> findByIsDeletedFalse();

    List<Document> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title);

    List<Document> findByUserIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(
            Long userId,
            String title
    );

    long countByUserIdAndIsDeletedFalse(Long userId);

    List<Document> findByFileTypeAndIsDeletedFalse(String fileType);

    @Query("SELECT d FROM Document d WHERE d.isEncrypted = true AND d.isDeleted = false")
    List<Document> findEncryptedDocuments();

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId " +
            "AND d.fileType = :fileType AND d.isDeleted = false")
    List<Document> findByUserAndFileType(
            @Param("userId") Long userId,
            @Param("fileType") String fileType
    );

    @Query("SELECT d FROM Document d WHERE d.fileSize > :sizeThreshold " +
            "AND d.isDeleted = false")
    List<Document> findLargeDocuments(@Param("sizeThreshold") Long sizeThreshold);
}
