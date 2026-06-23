package com.partha.document_analyzer.services;

import com.partha.document_analyzer.enums.DocumentPriority;
import com.partha.document_analyzer.model.ProcessingTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentProcessingService {

    // PriorityQueue for priority-based task processing
    private final PriorityQueue<ProcessingTask> taskQueue = new PriorityQueue<>();

    // ConcurrentHashMap for thread-safe task status tracking
    private final ConcurrentHashMap<Long, String> taskStatus = new ConcurrentHashMap<>();

    // LinkedList for maintaining processing history (FIFO with efficient add/remove)
    private final LinkedList<ProcessingTask> processingHistory =  new LinkedList<>();

    // Maximum history size
    private static final int MAX_HISTORY_SIZE = 100;

    public synchronized void queueTask(Long documentId, String documentTitle,
                                       String operation, DocumentPriority documentPriority, Long userId) {

        ProcessingTask task = new ProcessingTask(documentId, documentTitle,
                documentPriority, operation, userId);

        taskQueue.offer(task);
        taskStatus.put(documentId, "QUEUED");

        log.info("task queued for document {} with priority {} for operation{}", documentId, documentPriority, operation);
    }

    //  Get next high-priority task. PriorityQueue automatically returns highest priority task
    public synchronized ProcessingTask getNextTask() {
        ProcessingTask task = taskQueue.poll();

        if (task != null) {
            taskStatus.put(task.getDocumentId(), "PROCESSING");
            log.info("Processing task document: {} with priority: {} ", task.getDocumentId(), task.getPriority());
        }

        return task;
    }

    // Mark task as completed and add to history. Uses LinkedList for efficient insertion at head

    public synchronized void completeTask(ProcessingTask task) {
        taskStatus.put(task.getDocumentId(), "COMPLETED");

        // Add to front of history (most recent first)
        processingHistory.addFirst(task);

        if (processingHistory.size() > MAX_HISTORY_SIZE) {
            processingHistory.removeLast();
        }
    }

    // get current queue size

    public int getQueueSize() {
        return taskQueue.size();
    }

    // get all queue list
    public List<ProcessingTask> getQueuedTask() {
        PriorityQueue<ProcessingTask> tempQueue = new PriorityQueue<>(taskQueue);
        List<ProcessingTask> sortedTasks = new ArrayList<>();

        // Poll elements one by one - guaranteed priority order
        while (!tempQueue.isEmpty()) {
            sortedTasks.add(tempQueue.poll());
        }

        return sortedTasks;
    }

    // get task status
    public String getTaskStatus(Long documentId) {
        return taskStatus.getOrDefault(documentId, "NOT FOUND");
    }

    // get processing history (using limit bcz for showing limited list)
    public List<ProcessingTask> getProcessingHistory(int limit) {
        return processingHistory.stream().limit(limit).toList();
    }

    // clearing completed task from history
    public synchronized void clearHistory() {
        processingHistory.clear();
    }
}
