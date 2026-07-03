package com.partha.document_analyzer.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<Long, Bucket> userBucket = new ConcurrentHashMap<>();

    public Bucket resolveBucket(Long userId) {
        return userBucket.computeIfAbsent(userId, id -> newBucket());
    }

    public Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(4)
                .refillGreedy(4, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
