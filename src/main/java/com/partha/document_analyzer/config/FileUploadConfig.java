package com.partha.document_analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.upload")
@Data
public class FileUploadConfig {

    private List<String> allowedTypes;
    private int maxDocumentsPerUser;
}
