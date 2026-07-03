package com.partha.document_analyzer.component;

import com.partha.document_analyzer.model.ProcessingTask;
import com.partha.document_analyzer.services.DocumentProcessingService;
import com.partha.document_analyzer.services.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentQueueWorker {

    private final DocumentProcessingService documentProcessingService;
    private final DocumentService documentService;

    @Scheduled(fixedDelay = 5)
    public void processNextTask() {

        ProcessingTask task = documentProcessingService.getNextTask();

        if (task == null) {
            return;
        }

        log.info("Worker picked up task for document {} operation {}",
                task.getDocumentId(), task.getOperation());

        try {
            if ("ANALYZE".equals(task.getOperation())) {
                documentService.analyzeDocument(task.getDocumentId(), task.getUserId());
            }
            documentProcessingService.completeTask(task);
            log.info("Task completed for document {}", task.getDocumentId());
        } catch (Exception e) {
            log.error("Task failed for document {}: {}", task.getDocumentId(), e.getMessage());
            documentProcessingService.failTask(task);
        }

    }
}
