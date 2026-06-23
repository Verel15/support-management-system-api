package com.ticket.support_management_system_api.features.auth.service;

import com.ticket.support_management_system_api.config.JwtProperties;
import com.ticket.support_management_system_api.features.auth.entities.DeviceSession;
import com.ticket.support_management_system_api.features.auth.entities.RefreshToken;
import com.ticket.support_management_system_api.features.auth.repository.RefreshTokenRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshToken create(User user, DeviceSession session, String tokenValue) {
        long expirationMs = jwtProperties.getRefreshTokenExpiration();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMs / 1000);

        RefreshToken token = RefreshToken.builder()
                .tokenHash(hashToken(tokenValue))
                .user(user)
                .deviceSession(session)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();

        return refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshToken rotate(RefreshToken old, DeviceSession session, String newTokenValue) {
        old.setRevoked(true);
        RefreshToken saved = refreshTokenRepository.save(old);

        RefreshToken newToken = create(old.getUser(), session, newTokenValue);
        saved.setReplacedBy(newToken);
        refreshTokenRepository.save(saved);

        return newToken;
    }

    @Transactional
    public void revokeAllForUser(java.util.UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Transactional
    public void revokeAllForSession(java.util.UUID sessionId) {
        refreshTokenRepository.revokeAllByDeviceSessionId(sessionId);
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
