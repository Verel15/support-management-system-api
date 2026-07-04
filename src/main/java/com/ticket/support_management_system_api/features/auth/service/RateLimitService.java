package com.ticket.support_management_system_api.features.auth.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsumeLoginByIp(String ip) {
        return getBucket("ip:" + ip, 10, Duration.ofMinutes(1)).tryConsume(1);
    }

    public boolean tryConsumeLoginByEmail(String email) {
        return getBucket("email:" + email, 5, Duration.ofMinutes(10)).tryConsume(1);
    }

    public void resetLoginBucket(String email) {
        buckets.remove("email:" + email);
    }

    public boolean tryConsumeReauthByUser(java.util.UUID userId) {
        return getBucket("reauth:" + userId, 5, Duration.ofMinutes(10)).tryConsume(1);
    }

    public void resetReauthBucket(java.util.UUID userId) {
        buckets.remove("reauth:" + userId);
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
