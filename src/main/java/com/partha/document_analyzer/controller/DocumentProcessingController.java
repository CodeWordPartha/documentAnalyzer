package com.partha.document_analyzer.controller;

import com.partha.document_analyzer.dto.SuccessResponseDto;
import com.partha.document_analyzer.enums.DocumentPriority;
import com.partha.document_analyzer.model.ProcessingTask;
import com.partha.document_analyzer.services.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/processing")
public class DocumentProcessingController {

    private final DocumentProcessingService documentProcessingService;

    @PostMapping("/queue")
    public ResponseEntity<SuccessResponseDto> queueTask(@RequestParam Long documentId,
                                                        @RequestParam String documentTitle,
                                                        @RequestParam DocumentPriority documentPriority,
                                                        @RequestParam String operation,
                                                        @RequestParam Long userId) {

        documentProcessingService.queueTask(documentId, documentTitle, operation, documentPriority, userId);

        HashMap<String, Object> data = new HashMap<>();
        data.put("documentId", documentId);
        data.put("priority", documentPriority);
        data.put("status", "QUEUED");
        data.put("operation", operation);

        SuccessResponseDto successResponseDto = new SuccessResponseDto("Task queued successfully", data);

        return ResponseEntity.ok(successResponseDto);

    }

    @GetMapping("/next")
    public ResponseEntity<?> getNextTask() {
        ProcessingTask task = documentProcessingService.getNextTask();

        if (task == null) {
            return ResponseEntity.ok(new SuccessResponseDto("Queue is empty"));
        }

        return ResponseEntity.ok(task);
    }

    @PostMapping("/complete")
    public ResponseEntity<SuccessResponseDto> completeTask(@RequestParam Long documentId,
                                                           @RequestParam String documentTitle,
                                                           @RequestParam DocumentPriority documentPriority,
                                                           @RequestParam String operation,
                                                           @RequestParam Long userId) {
        ProcessingTask task = new ProcessingTask(documentId, documentTitle, documentPriority, operation, userId);

        documentProcessingService.completeTask(task);
        SuccessResponseDto responseDto = new SuccessResponseDto("Task is completed");

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/queue-size")
    public ResponseEntity<SuccessResponseDto> getQueueSize() {
        int queueSize = documentProcessingService.getQueueSize();

        HashMap<String, Object> data = new HashMap<>();
        data.put("queueSize", queueSize);

        SuccessResponseDto responseDto = new SuccessResponseDto(queueSize + "task in queue", data);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/queue")
    public ResponseEntity<List<ProcessingTask>> getQueuedTask() {
        List<ProcessingTask> queuedTask = documentProcessingService.getQueuedTask();

        return ResponseEntity.ok(queuedTask);
    }

    @GetMapping("/status/{documentId}")
    public ResponseEntity<SuccessResponseDto> getTaskStatus(@PathVariable Long documentId) {

        String taskStatus = documentProcessingService.getTaskStatus(documentId);
        Map<String, Object> data = new HashMap<>();
        data.put("documentId", documentId);
        data.put("status", taskStatus);

        SuccessResponseDto responseDto = new SuccessResponseDto("Task status retreived", data);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ProcessingTask>> getHistory(@RequestParam(defaultValue = "10") int limit) {

        List<ProcessingTask> history = documentProcessingService.getProcessingHistory(limit);

        return ResponseEntity.ok(history);

    }

    @DeleteMapping("/history")
    public ResponseEntity<SuccessResponseDto> clearHistory() {
        documentProcessingService.clearHistory();

        SuccessResponseDto responseDto = new SuccessResponseDto("Processing history is cleared successfully");

        return ResponseEntity.ok(responseDto);
    }

}
