package com.partha.document_analyzer.model;

import com.partha.document_analyzer.enums.DocumentPriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingTask implements Comparable<ProcessingTask> {

    private Long documentId;
    private String documentTitle;
    private DocumentPriority priority;
    private String operation;  // "SUMMARIZE", "ENCRYPT", "ANALYZE"
    private Long userId;
    private LocalDateTime submittedAt;

    public ProcessingTask(Long documentId, String documentTitle, DocumentPriority priority,
                          String operation, Long userId) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.priority = priority;
        this.operation = operation;
        this.userId = userId;
        this.submittedAt = LocalDateTime.now();
    }


    @Override
    public int compareTo(ProcessingTask other) {

        int priorityCompare = Integer.compare(other.priority.getValue(), this.priority.getValue());
        // Sorting is always decided from the point of view of this.
        //You control order by deciding what you compare against this.
        // (this, other) means ascending order
        // (other, this) means descending order here we want descending bcz high priority has high no.

        if (priorityCompare == 0) {
            return this.submittedAt.compareTo(other.submittedAt);
            // If priority is same, earlier submitted task should come first thats why 'this' is written first
        }
        return priorityCompare;
    }
}
