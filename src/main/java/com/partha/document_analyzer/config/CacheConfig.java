package com.partha.document_analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cache")
@Data
public class CacheConfig {

    private AiAnalysis aiAnalysis = new AiAnalysis();
    private DocumentList documentList = new DocumentList();
    private Prefix prefix = new Prefix();

    @Data
    public static class AiAnalysis {
        private long ttlHours;
    }

    @Data
    public static class DocumentList {
        private long ttlMinutes;
    }

    @Data
    public static class Prefix {
        private String aiAnalysis;
        private String documentList;
    }

}
