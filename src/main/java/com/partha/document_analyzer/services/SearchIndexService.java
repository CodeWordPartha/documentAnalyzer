package com.partha.document_analyzer.services;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchIndexService {

    @Value("classpath:stop-words/stop-words.txt")
    private Resource stopWordsResource;

    private Set<String> stopWords;

    @PostConstruct
    public void init() {
        try {
            stopWords = new HashSet<>(Arrays.asList(
                    stopWordsResource.getContentAsString(StandardCharsets.UTF_8)
                            .split("\\r?\\n")
            ));
            log.info("Loaded {} stop words", stopWords.size());
        } catch (IOException e) {
            log.error("Failed to load stop words", e);
            stopWords = new HashSet<>();
        }
    }

    private final Map<Long, DocumentIndex> documentCache = new HashMap<>();

    private final TreeMap<Long, Set<Long>> dateIndex = new TreeMap<>();

    private final Map<Long, Map<String, Set<Long>>> userKeywordIndex = new ConcurrentHashMap<>();

    private final Map<Long, Map<String, Integer>> userKeywordFrequencyMap = new ConcurrentHashMap<>();

    public void indexDocument(Long docId, String title, String content, long timestamp, Long userId) {

        Set<String> keywords = extractKeywords(title + " " + content);

        documentCache.put(docId, new DocumentIndex(docId, title, keywords));

        dateIndex.computeIfAbsent(timestamp, k -> new HashSet<>()).add(docId);

        Map<String, Set<Long>> kwIndex = userKeywordIndex.computeIfAbsent(userId, k -> new HashMap<>());

        Map<String, Integer> freqMap = userKeywordFrequencyMap.computeIfAbsent(userId, k -> new HashMap<>());

        for (String keyword : keywords) {
            kwIndex.computeIfAbsent(keyword, k -> new HashSet<>()).add(docId);
            freqMap.merge(keyword, 1, Integer::sum);
        }

        log.info("Indexed document {} with {} keywords for user {}", docId, keywords.size(), userId);
    }

    public Set<Long> searchByKeywords(List<String> keywords, Long userId) {

        Map<String, Set<Long>> kwIndex = userKeywordIndex.getOrDefault(userId, new HashMap<>());

        if (keywords.isEmpty()) {
            return Collections.emptySet();
        }

        Set<Long> results = new HashSet<>(
                kwIndex.getOrDefault(keywords.get(0).toLowerCase(), Collections.emptySet())
        );

        for (int i = 1; i < keywords.size(); i++) {
            Set<Long> keywordDocs = kwIndex.getOrDefault(
                    keywords.get(i).toLowerCase(),
                    Collections.emptySet()
            );
            results.retainAll(keywordDocs);
        }

        log.info("Search for {} returned {} documents for user {}", keywords, results.size(), userId);
        return results;

    }

    public Set<Long> getDocumentsBetweenDates(long startTime, long endTime) {
        return dateIndex.subMap(startTime, true, endTime, true)
                .values()
                .stream()
                .flatMap(docSet -> docSet.stream())
                .collect(Collectors.toSet());
    }

    public List<String> getTopKeywords(int n, Long userId) {
        Map<String, Integer> freqMap = userKeywordFrequencyMap
                .getOrDefault(userId, new HashMap<>());

        return freqMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

    }

    public void removeDocument(Long docId) {
        DocumentIndex doc = documentCache.remove(docId);

        if (doc != null) {
            userKeywordIndex.forEach((userId, kwIndex) -> {
                for (String keyword : doc.getKeywords()) {
                    Set<Long> docs = kwIndex.get(keyword);
                    if (docs != null) {
                        docs.remove(docId);
                        if (docs.isEmpty()) {
                            kwIndex.remove(keyword);
                        }
                    }
                }

                userKeywordFrequencyMap.getOrDefault(userId, new HashMap<>())
                        .entrySet()
                        .removeIf(entry -> doc.getKeywords().contains(entry.getKey())
                                && !kwIndex.containsKey(entry.getKey()));
            });

            dateIndex.values().forEach(docSet -> docSet.remove(docId));

            log.info("Removed document {} from index", docId);
        }
    }

    public Map<String, Object> getIndexStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", documentCache.size());
        stats.put("totalUsers", userKeywordIndex.size());
        stats.put("totalKeywords", userKeywordIndex.values().stream()
                .mapToInt(Map::size).sum());
        return stats;
    }

    public synchronized void clearIndex() {
        documentCache.clear();
        dateIndex.clear();
        userKeywordIndex.clear();
        userKeywordFrequencyMap.clear();
        log.info("Search index cleared");
    }

    // Helper methods

    private Set<String> extractKeywords(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(word -> word.length() > 3)
                .filter(word -> !word.matches(".*\\d.*"))
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.toSet());
    }



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
