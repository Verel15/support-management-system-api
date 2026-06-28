package com.ticket.support_management_system_api.features.upload.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UploadRateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsumeUpload(String userId) {
        return getBucket("upload:" + userId, 20, Duration.ofMinutes(1)).tryConsume(1);
    }

    private Bucket getBucket(String key, long capacity, Duration refillPeriod) {
        return buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillIntervally(capacity, refillPeriod)
                        .build())
                .build());
    }
}
