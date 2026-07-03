package com.partha.document_analyzer.component;

import com.partha.document_analyzer.entities.Document;
import com.partha.document_analyzer.repositories.DocumentRepository;
import com.partha.document_analyzer.services.SearchIndexService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchIndexStartupLoader {

    private final DocumentRepository documentRepository;
    private final SearchIndexService searchIndexService;

    @PostConstruct
    public void loadIndexOnStartup() {
        log.info("Rebuilding search index from DB on startup...");

        List<Document> documents = documentRepository.findByIsDeletedFalse();

        int indexed = 0;
        for (Document doc : documents) {
            if (doc.getContent() != null && !doc.getContent().isEmpty()) {
                searchIndexService.indexDocument(
                        doc.getId(),
                        doc.getTitle(),
                        doc.getContent(),
                        doc.getCreatedAt()
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli(),
                        doc.getUser().getId()
                );
                indexed++;
            }
        }

        log.info("Search index rebuilt with {} documents on startup", indexed);
    }
}