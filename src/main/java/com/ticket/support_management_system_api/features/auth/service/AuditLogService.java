package com.ticket.support_management_system_api.features.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.support_management_system_api.features.auth.entities.AuthAuditLog;
import com.ticket.support_management_system_api.features.auth.enums.EAuditEvent;
import com.ticket.support_management_system_api.features.auth.repository.AuthAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuthAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async("auditLogExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(EAuditEvent event, UUID userId, HttpServletRequest request, UUID deviceSessionId, Map<String, Object> metadata) {
        try {
            AuthAuditLog entry = AuthAuditLog.builder()
                    .userId(userId)
                    .event(event)
                    .ipAddress(extractClientIp(request))
                    .userAgent(truncate(request.getHeader("User-Agent"), 500))
                    .deviceSessionId(deviceSessionId)
                    .metadata(toJson(metadata))
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to save audit log event={} userId={}", event, userId, e);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
