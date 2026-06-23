package com.ticket.support_management_system_api.features.auth.service;

import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.auth.entities.DeviceSession;
import com.ticket.support_management_system_api.features.auth.repository.DeviceSessionRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceSessionService {

    private final DeviceSessionRepository deviceSessionRepository;

    @Transactional
    public DeviceSession create(User user, HttpServletRequest request) {
        DeviceSession session = DeviceSession.builder()
                .user(user)
                .deviceName(extractDeviceName(request))
                .ipAddress(extractClientIp(request))
                .userAgent(truncate(request.getHeader("User-Agent"), 500))
                .lastActiveAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        return deviceSessionRepository.save(session);
    }

    @Transactional
    public void updateLastActive(DeviceSession session) {
        session.setLastActiveAt(LocalDateTime.now());
        deviceSessionRepository.save(session);
    }

    @Transactional
    public void revoke(UUID sessionId, UUID userId) {
        DeviceSession session = deviceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบ session id: " + sessionId));
        if (!session.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("ไม่พบ session id: " + sessionId);
        }
        session.setActive(false);
        deviceSessionRepository.save(session);
    }

    @Transactional
    public void revokeAllExcept(UUID userId, UUID exceptSessionId) {
        deviceSessionRepository.deactivateAllExcept(userId, exceptSessionId);
    }

    @Transactional
    public void revokeAll(UUID userId) {
        deviceSessionRepository.deactivateAll(userId);
    }

    @Transactional(readOnly = true)
    public List<DeviceSession> findAllActive(UUID userId) {
        return deviceSessionRepository.findByUserIdAndActiveTrue(userId);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractDeviceName(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        if (ua == null) return "Unknown Device";
        if (ua.contains("Edg"))     return "Edge";
        if (ua.contains("Chrome"))  return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari"))  return "Safari";
        if (ua.contains("Postman")) return "Postman";
        return "Unknown Browser";
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
