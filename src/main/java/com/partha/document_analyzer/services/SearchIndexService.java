package com.partha.document_analyzer.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchIndexService {

    // HashMap for O(1) document lookup by ID. it is used to find document with document id
    private final Map<Long, DocumentIndex> documentCache = new HashMap<>();

    // TreeMap for sorted date-based queries (range queries)
    private final TreeMap<Long, Set<Long>> dateIndex = new TreeMap<>();

    // Inverted index: keyword → set of document IDs. It is used to search the keyword is present in which (multiple) documents
    // why set ? bcz we dont want to store duplicate document ids
    private final Map<String, Set<Long>> keywordIndex = new HashMap<>();

    // TreeSet for maintaining top keywords (sorted by frequency)
    private final TreeSet<KeywordFrequency> popularKeywords = new TreeSet<>(
            Comparator.comparing(
                            (KeywordFrequency kf) -> kf.getFrequency()
                    )
                    .reversed()
                    .thenComparing(
                            (KeywordFrequency kf) -> kf.getKeyword()
                    )
    );

    // Track keyword frequencies. We must count how many times each keyword appears
    // we need this to get top keywords
    private final Map<String, Integer> keywordFrequencyMap = new HashMap<>();

    public void indexDocument(Long docId, String title, String content, long timestamp) {
        // Extract keywords from content
        Set<String> keywords = extractKeywords(title + " " + content);

        // Store in cache (HashMap for O(1) lookup)
        documentCache.put(docId, new DocumentIndex(docId, title, keywords));

        // Add to date index (TreeMap for sorted dates and range queries)
        dateIndex.computeIfAbsent(timestamp, k -> new HashSet<>()).add(docId);

        // Build inverted index (HashSet ensures unique doc IDs per keyword)
        for (String keyword : keywords) {
            keywordIndex.computeIfAbsent(keyword, k -> new HashSet<>()).add(docId);

            // Update keyword frequency
            keywordFrequencyMap.merge(keyword, 1, Integer::sum);
        }

        // Update popular keywords (TreeSet maintains sorted order)
        updatePopularKeywords();

        log.info("Indexed document {} with {} keywords", docId, keywords.size());
    }

    // Search by keywords (AND operation). Uses HashSet for efficient intersection
    public Set<Long> searchByKeywords(List<String> keywords) {
        if (keywords.isEmpty()) {
            return Collections.emptySet();
        }

        // Start with first keyword's document set
        Set<Long> results = new HashSet<>(
                keywordIndex.getOrDefault(keywords.get(0).toLowerCase(), Collections.emptySet())
        );

        // Intersect with remaining keywords (documents must have ALL keywords)
        for (int i = 1; i < keywords.size(); i++) {
            Set<Long> keywordDocs = keywordIndex.getOrDefault(
                    keywords.get(i).toLowerCase(),
                    Collections.emptySet()
            );
            results.retainAll(keywordDocs);  // Intersection
        }

        log.info("Search for {} returned {} documents", keywords, results.size());
        return results;
    }

    // Get documents within date range. Uses TreeMap's subMap for efficient range queries

    public Set<Long> getDocumentsBetweenDates(long startTime, long endTime) {
        return dateIndex.subMap(startTime, true, endTime, true)
                .values()
                .stream()
                .flatMap(docSet -> docSet.stream())
                .collect(Collectors.toSet());
    }

    // Get top N popular keywords. TreeSet already maintains sorted order

    public List<String> getTopKeywords(int n) {
        return popularKeywords.stream()
                .limit(n)
                .map(kf -> kf.getKeyword())
                .collect(Collectors.toList());
    }

    // Remove document from index

    public void removeDocument(Long docId) {
        DocumentIndex doc = documentCache.remove(docId);

        if (doc != null) {
            // Remove from keyword index
            for (String keyword : doc.getKeywords()) {
                Set<Long> docs = keywordIndex.get(keyword);
                if (docs != null) {
                    docs.remove(docId);
                    if (docs.isEmpty()) {
                        keywordIndex.remove(keyword);
                    }
                }
            }

            // Remove from date index
            dateIndex.values().forEach(docSet -> docSet.remove(docId));

            log.info("Removed document {} from index", docId);
        }
    }

    // Get index statistics
    public Map<String, Object> getIndexStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", documentCache.size());
        stats.put("totalKeywords", keywordIndex.size());
        stats.put("dateRanges", dateIndex.size());
        return stats;
    }

    public void clearIndex() {
        documentCache.clear();
        dateIndex.clear();
        keywordIndex.clear();
        popularKeywords.clear();
        keywordFrequencyMap.clear();
        log.info("Search index cleared");
    }

    // Helper methods

    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(word -> word.length() > 3)  // Only words > 3 chars
                .collect(Collectors.toSet());  // HashSet for unique keywords
    }

    private void updatePopularKeywords() {
        popularKeywords.clear();
        keywordFrequencyMap.forEach((keyword, frequency) ->
                popularKeywords.add(new KeywordFrequency(keyword, frequency))
        );
    }

    // Inner classes

    @Data
    @AllArgsConstructor
    private static class DocumentIndex {
        private Long id;
        private String title;
        private Set<String> keywords;
    }

    @Data
    @AllArgsConstructor
    private static class KeywordFrequency {
        private String keyword;
        private Integer frequency;
    }
}
