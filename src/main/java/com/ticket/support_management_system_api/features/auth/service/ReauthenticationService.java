package com.ticket.support_management_system_api.features.auth.service;

import com.ticket.support_management_system_api.common.exception.AccountLockedException;
import com.ticket.support_management_system_api.common.exception.AuthException;
import com.ticket.support_management_system_api.common.exception.ReauthenticationFailedException;
import com.ticket.support_management_system_api.features.auth.enums.AuditEvent;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReauthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final RateLimitService rateLimitService;

    public void verifyPassword(UUID userId, String rawPassword, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("ไม่พบผู้ใช้งาน"));

        if (user.getReauthLockedUntil() != null && user.getReauthLockedUntil().isAfter(LocalDateTime.now())) {
            auditLogService.log(AuditEvent.REAUTH_FAILED, userId, request, null,
                    Map.of("reason", "reauth_locked"));
            throw new AccountLockedException("ยืนยันตัวตนผิดหลายครั้งเกินไป กรุณาลองใหม่ภายหลัง");
        }

        if (!rateLimitService.tryConsumeReauthByUser(userId)) {
            user.setReauthLockedUntil(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);
            auditLogService.log(AuditEvent.REAUTH_LOCKED, userId, request, null, Map.of());
            throw new AccountLockedException("ยืนยันรหัสผ่านผิดหลายครั้งเกินไป กรุณาลองใหม่ภายหลัง");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            auditLogService.log(AuditEvent.REAUTH_FAILED, userId, request, null,
                    Map.of("reason", "wrong_password"));
            throw new ReauthenticationFailedException("รหัสผ่านไม่ถูกต้อง");
        }

        rateLimitService.resetReauthBucket(userId);
        if (user.getReauthLockedUntil() != null) {
            user.setReauthLockedUntil(null);
            userRepository.save(user);
        }
        auditLogService.log(AuditEvent.REAUTH_SUCCESS, userId, request, null, Map.of());
    }
}
