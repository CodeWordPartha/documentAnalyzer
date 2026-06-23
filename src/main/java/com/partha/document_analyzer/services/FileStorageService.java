package com.partha.document_analyzer.services;

import com.partha.document_analyzer.config.FileStorageConfig;
import com.partha.document_analyzer.exceptions.InvalidFileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    public void init() {
        try {
            Path uploadPath = Paths.get(fileStorageConfig.getUploadDirectory());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath); // it will create directory in that path
                log.info("Created upload directory: {}", uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Get original filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Generate unique filename to avoid conflicts
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            // Create upload directory if not exists
            init();

            // Destination path
            Path uploadPath = Paths.get(fileStorageConfig.getUploadDirectory());
            Path destinationPath = uploadPath.resolve(uniqueFilename);

            // Copy file to destination
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored: {} -> {}", originalFilename, uniqueFilename);

            return uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFilename, e);
        }
    }

    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = Paths.get(fileStorageConfig.getUploadDirectory()).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filename);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + filename, e);
        }
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = Paths.get(fileStorageConfig.getUploadDirectory()).resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", filename);

        } catch (IOException e) {
            log.error("Failed to delete file: {}", filename, e);
        }
    }

    public long getFileSize(MultipartFile file) {
        return file.getSize();
    }

    public String getContentType(MultipartFile file) {
        return file.getContentType();
    }

    private void validateFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new InvalidFileException("Cannot upload empty file");
        }

        long maxSize = 10 * 1024 * 1024;  // 10MB
        if (file.getSize() > maxSize) {
            throw new InvalidFileException("File size exceeds maximum limit of 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new InvalidFileException(
                    "Invalid file type. Allowed types: PDF, TXT, DOCX, DOC"
            );
        }
    }

    private boolean isAllowedContentType(String contentType) {
        return contentType.equals("application/pdf") ||
                contentType.equals("text/plain") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("application/msword");
    }
}