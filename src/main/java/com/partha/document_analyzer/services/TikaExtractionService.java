package com.partha.document_analyzer.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class TikaExtractionService {

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) {
        try {
            log.info("Extracting text from the file {}", file.getOriginalFilename());
            String extractedText = tika.parseToString(file.getInputStream());
            log.info("Successfully extracted the charachters {}", extractedText.length());
            return extractedText;
        } catch (Exception e) {
            log.error("failed to extract text from the file {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to extract text from file", e);
        }
    }
}
