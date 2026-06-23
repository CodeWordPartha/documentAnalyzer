package com.partha.document_analyzer.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FileStorageConfig {

    @Value("${file.upload-dir}")
    private String uploadDirectory;
}
