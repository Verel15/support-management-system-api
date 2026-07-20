package com.ticket.support_management_system_api.features.auth.service;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.enums.CommonStatus;
import com.ticket.support_management_system_api.common.exception.AccountLockedException;
import com.ticket.support_management_system_api.common.exception.AuthException;
import com.ticket.support_management_system_api.common.exception.TooManyRequestsException;
import com.ticket.support_management_system_api.config.JwtProperties;
import com.ticket.support_management_system_api.features.auth.dto.LoginRequest;
import com.ticket.support_management_system_api.features.auth.dto.LoginResponse;
import com.ticket.support_management_system_api.features.auth.dto.TokenRefreshRequest;
import com.ticket.support_management_system_api.features.auth.dto.TokenRefreshResponse;
import com.ticket.support_management_system_api.features.auth.entities.DeviceSession;
import com.ticket.support_management_system_api.features.auth.entities.RefreshToken;
import com.ticket.support_management_system_api.features.auth.enums.EAuditEvent;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.repository.RefreshTokenRepository;
import com.ticket.support_management_system_api.features.user.repository.StaffDetailsRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user_type.entities.UserType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final UserRepository userRepository;
    private final StaffDetailsRepository staffDetailsRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final DeviceSessionService deviceSessionService;
    private final AuditLogService auditLogService;
    private final RateLimitService rateLimitService;
    private final JwtProperties jwtProperties;

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String email = request.getEmail();
        String ip = extractClientIp(httpRequest);

        if (!rateLimitService.tryConsumeLoginByIp(ip)) {
            auditLogService.log(EAuditEvent.LOGIN_FAILED, null, httpRequest, null,
                    Map.of("reason", "ip_rate_limit", "email", email));
            throw new TooManyRequestsException("พยายามเข้าสู่ระบบมากเกินไป กรุณาลองใหม่ภายหลัง");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            auditLogService.log(EAuditEvent.LOGIN_FAILED, null, httpRequest, null,
                    Map.of("reason", "user_not_found", "email", email));
            throw new AuthException("อีเมลหรือรหัสผ่านไม่ถูกต้อง");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            auditLogService.log(EAuditEvent.LOGIN_FAILED, user.getId(), httpRequest, null,
                    Map.of("reason", "account_locked"));
            throw new AccountLockedException("บัญชีถูกล็อคชั่วคราว กรุณาลองใหม่ภายหลัง");
        }

        if (!rateLimitService.tryConsumeLoginByEmail(email)) {
            lockAccount(user, 30);
            auditLogService.log(EAuditEvent.ACCOUNT_LOCKED, user.getId(), httpRequest, null, Map.of());
            throw new AccountLockedException("บัญชีถูกล็อคชั่วคราวเนื่องจากพยายามเข้าสู่ระบบหลายครั้งเกินไป");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int attempts = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(attempts);
            userRepository.save(user);
            auditLogService.log(EAuditEvent.LOGIN_FAILED, user.getId(), httpRequest, null,
                    Map.of("reason", "wrong_password", "attempts", attempts));
            throw new AuthException("อีเมลหรือรหัสผ่านไม่ถูกต้อง");
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setStatus(CommonStatus.ACTIVE);
        userRepository.save(user);
        rateLimitService.resetLoginBucket(email);

        UserType userType = resolveUserType(user);
        DeviceSession session = deviceSessionService.create(user, httpRequest);

        String refreshTokenValue = jwtService.generateRefreshTokenValue();
        refreshTokenService.create(user, session, refreshTokenValue);

        String accessToken = jwtService.generateAccessToken(user, userType);
        setRefreshTokenCookie(httpResponse, refreshTokenValue);

        auditLogService.log(EAuditEvent.LOGIN_SUCCESS, user.getId(), httpRequest, session.getId(), Map.of());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .accountType(user.getAccountType())
                .build();
    }

    @Transactional
    public TokenRefreshResponse refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                        TokenRefreshRequest body) {
        String tokenValue = extractRefreshToken(httpRequest, body);

        if (tokenValue == null) {
            throw new AuthException("ไม่พบ refresh token");
        }

        String tokenHash = refreshTokenService.hashToken(tokenValue);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("Refresh token ไม่ถูกต้อง"));

        if (refreshToken.isRevoked()) {
            refreshTokenService.revokeAllForUser(refreshToken.getUser().getId());
            auditLogService.log(EAuditEvent.SUSPICIOUS_REUSE, refreshToken.getUser().getId(),
                    httpRequest, null, Map.of("tokenId", refreshToken.getId().toString()));
            throw new AuthException("ตรวจพบการใช้งานที่ผิดปกติ กรุณาเข้าสู่ระบบใหม่");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException("Refresh token หมดอายุ กรุณาเข้าสู่ระบบใหม่");
        }

        DeviceSession session = refreshToken.getDeviceSession();
        if (session == null || !session.isActive()) {
            throw new AuthException("Session ถูกยกเลิก กรุณาเข้าสู่ระบบใหม่");
        }

        User user = refreshToken.getUser();
        UserType userType = resolveUserType(user);

        String newRefreshTokenValue = jwtService.generateRefreshTokenValue();
        refreshTokenService.rotate(refreshToken, session, newRefreshTokenValue);
        deviceSessionService.updateLastActive(session);

        String newAccessToken = jwtService.generateAccessToken(user, userType);
        setRefreshTokenCookie(httpResponse, newRefreshTokenValue);

        auditLogService.log(EAuditEvent.TOKEN_REFRESH, user.getId(), httpRequest, session.getId(), Map.of());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Transactional
    public void logout(JwtPrincipal principal, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String tokenValue = extractRefreshToken(httpRequest, null);

        if (tokenValue != null) {
            String tokenHash = refreshTokenService.hashToken(tokenValue);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);

                DeviceSession session = rt.getDeviceSession();
                UUID sessionId = session != null ? session.getId() : null;

                if (session != null) {
                    refreshTokenService.revokeAllForSession(session.getId());
                    deviceSessionService.revoke(session.getId(), principal.userId());
                }

                auditLogService.log(EAuditEvent.LOGOUT, principal.userId(), httpRequest, sessionId, Map.of());
            });
        } else {
            auditLogService.log(EAuditEvent.LOGOUT, principal.userId(), httpRequest, null, Map.of());
        }

        clearRefreshTokenCookie(httpResponse);
    }

    @Transactional
    public void revokeSession(UUID sessionId, JwtPrincipal principal, HttpServletRequest httpRequest) {
        refreshTokenService.revokeAllForSession(sessionId);
        deviceSessionService.revoke(sessionId, principal.userId());
        auditLogService.log(EAuditEvent.SESSION_REVOKED, principal.userId(), httpRequest,
                sessionId, Map.of("sessionId", sessionId.toString()));
    }

    @Transactional
    public void revokeAllSessions(JwtPrincipal principal, HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse, UUID currentSessionId) {
        refreshTokenService.revokeAllForUser(principal.userId());
        if (currentSessionId != null) {
            deviceSessionService.revokeAllExcept(principal.userId(), currentSessionId);
        } else {
            deviceSessionService.revokeAll(principal.userId());
        }
        clearRefreshTokenCookie(httpResponse);
        auditLogService.log(EAuditEvent.ALL_SESSIONS_REVOKED, principal.userId(), httpRequest, null, Map.of());
    }

    private UserType resolveUserType(User user) {
        if (user.getAccountType() == AccountType.STAFF) {
            return staffDetailsRepository.findById(user.getId())
                    .map(sd -> sd.getUserType())
                    .orElse(null);
        }
        return null;
    }

    private void lockAccount(User user, int lockMinutes) {
        user.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
        userRepository.save(user);
    }

    private String extractRefreshToken(HttpServletRequest request, TokenRefreshRequest body) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        if (body != null && body.getRefreshToken() != null) {
            return body.getRefreshToken();
        }
        return null;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String tokenValue) {
        long maxAgeSeconds = jwtProperties.getRefreshTokenExpiration() / 1000;
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, tokenValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge((int) maxAgeSeconds);
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
