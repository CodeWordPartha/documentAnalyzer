package com.partha.document_analyzer.controller;

import com.partha.document_analyzer.dto.SuccessResponseDto;
import com.partha.document_analyzer.services.SearchIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/search-index")
@RequiredArgsConstructor
public class SearchIndexController {

    private final SearchIndexService searchIndexService;

    @PostMapping("/index")
    public ResponseEntity<SuccessResponseDto> indexDocument(@RequestParam Long documentId,
                                                            @RequestParam String title,
                                                            @RequestParam String content,
                                                            @RequestParam Long userId,
                                                            @RequestParam(required = false) Long timeStamp) {
        long indexTimeStamp = timeStamp != null ? timeStamp : System.currentTimeMillis();

        searchIndexService.indexDocument(documentId, title, content, indexTimeStamp, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("documentId", documentId);
        data.put("indexed", true);

        SuccessResponseDto responseDto = new SuccessResponseDto("Document indexed succesfully", data);

        return ResponseEntity.ok(responseDto);

    }

    @GetMapping("/search")
    public ResponseEntity<SuccessResponseDto> search(@RequestParam List<String> keywords, @RequestParam Long userId) {
        Set<Long> documentIds = searchIndexService.searchByKeywords(keywords, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("documentIds", documentIds);
        data.put("keywords", keywords);
        data.put("count", documentIds.size());

        SuccessResponseDto responseDto = new SuccessResponseDto("Found" + documentIds.size() + "documents", data);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/date-range")
    public ResponseEntity<SuccessResponseDto> getDocumentByDateRange( @RequestParam String startDate,
                                                                      @RequestParam String endDate) {
        long startTimeStamp = LocalDate.parse(startDate).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTimeStamp = LocalDate.parse(endDate).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Set<Long> documentIds = searchIndexService.getDocumentsBetweenDates(startTimeStamp, endTimeStamp);

        Map<String, Object> data = new HashMap<>();
        data.put("documentIds", documentIds);
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        data.put("count", documentIds.size());

        SuccessResponseDto responseDto = new SuccessResponseDto("found" + documentIds.size() + "documents in this date range", data);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/top-keywords")
    public ResponseEntity<SuccessResponseDto> getTopKeywords(
            @RequestParam(defaultValue = "10") int limit, @RequestParam Long userId) {

        List<String> topKeywords = searchIndexService.getTopKeywords(limit, userId);

        Map<String, Object> data = new HashMap<>();
        data.put("topKeywords", topKeywords);
        data.put("count", topKeywords.size());

        SuccessResponseDto response = new SuccessResponseDto(
                "Retrieved top " + topKeywords.size() + " keywords",
                data
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<SuccessResponseDto> removeDocument(@PathVariable Long documentId) {
        searchIndexService.removeDocument(documentId);

        SuccessResponseDto response = new SuccessResponseDto(
                "Document removed from index"
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIndexStats() {
        Map<String, Object> stats = searchIndexService.getIndexStats();
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping
    public ResponseEntity<SuccessResponseDto> clearIndex() {
        searchIndexService.clearIndex();

        SuccessResponseDto response = new SuccessResponseDto(
                "Search index cleared"
        );

        return ResponseEntity.ok(response);
    }

}

