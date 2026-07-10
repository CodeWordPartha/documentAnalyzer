package com.partha.document_analyzer;

import com.partha.document_analyzer.config.CacheConfig;
import com.partha.document_analyzer.config.FileUploadConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({FileUploadConfig.class, CacheConfig.class})
@EnableScheduling
public class DocumentAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentAnalyzerApplication.class, args);
	}

}
