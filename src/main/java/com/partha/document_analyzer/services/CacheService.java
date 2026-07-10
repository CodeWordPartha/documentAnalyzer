package com.partha.document_analyzer.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.partha.document_analyzer.config.CacheConfig;
import com.partha.document_analyzer.dto.DocumentDetailResponseDto;
import com.partha.document_analyzer.dto.DocumentResponseDto;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheConfig cacheConfig;

    public void cacheAiAnalysis(Long documentId, DocumentDetailResponseDto documentDetailResponseDto) {
        String key = cacheConfig.getPrefix().getAiAnalysis() + documentId;
        redisTemplate.opsForValue().set(key, documentDetailResponseDto, cacheConfig.getAiAnalysis().getTtlHours(), TimeUnit.HOURS);
        log.info("Cached AI analysis for document {}", documentId);
    }

    public DocumentDetailResponseDto getCacheAiAnalysis(Long documentId) {
        String key = cacheConfig.getPrefix().getAiAnalysis() + documentId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            log.info("Cache HIT for AI analysis document {}", documentId);
            return convertToDto(cached, DocumentDetailResponseDto.class);
        }

        log.info("Cache MISS for AI analysis document {}", documentId);
        return null;
    }

    public void invalidateAiAnalysis(Long documentId) {
        String key = cacheConfig.getPrefix().getAiAnalysis() + documentId;
        redisTemplate.delete(key);
        log.info("Invalidated AI analysis cache for document {}", documentId);
    }

    public void cacheDocumentList(Long userId, List<DocumentResponseDto> documents) {
        String key = cacheConfig.getPrefix().getDocumentList() + userId;

        redisTemplate.opsForValue().set(key, documents,
                cacheConfig.getDocumentList().getTtlMinutes(), TimeUnit.MINUTES);
    }

    public List<DocumentResponseDto> getCachedDocumentList(Long userId) {
        String key = cacheConfig.getPrefix().getDocumentList() + userId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            log.info("Cache HIT for document list user {}", userId);
            return convertToList(cached, DocumentResponseDto.class);
        }

        log.info("Cache MISS for document list user {}", userId);
        return null;
    }

    public void invalidateDocumentList(Long userId) {
        String key = cacheConfig.getPrefix().getDocumentList() + userId;
        redisTemplate.delete(key);
        log.info("Invalidated document list cache for user {}", userId);
    }

    public <T> List<T> convertToList(Object cached, Class<T> targetClass) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        return mapper.convertValue(cached,
                mapper.getTypeFactory().constructCollectionType(List.class, targetClass));
    }

    public <T> T convertToDto(Object cached, Class<T> targetClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper.convertValue(cached, targetClass);
    }

}
