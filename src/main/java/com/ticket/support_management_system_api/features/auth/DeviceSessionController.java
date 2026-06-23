package com.ticket.support_management_system_api.features.auth;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.features.auth.dto.DeviceSessionResponse;
import com.ticket.support_management_system_api.features.auth.entities.DeviceSession;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.service.DeviceSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/sessions")
@RequiredArgsConstructor
public class DeviceSessionController {

    private final AuthService authService;
    private final DeviceSessionService deviceSessionService;

    @GetMapping
    ResponseEntity<ApiResponse<List<DeviceSessionResponse>>> listSessions(
            @AuthenticationPrincipal JwtPrincipal principal) {
        List<DeviceSession> sessions = deviceSessionService.findAllActive(principal.userId());
        List<DeviceSessionResponse> response = sessions.stream()
                .map(s -> DeviceSessionResponse.builder()
                        .id(s.getId())
                        .deviceName(s.getDeviceName())
                        .ipAddress(s.getIpAddress())
                        .lastActiveAt(s.getLastActiveAt())
                        .createdAt(s.getCreatedAt())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{sessionId}")
    ResponseEntity<ApiResponse<Void>> revokeSession(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal JwtPrincipal principal,
            HttpServletRequest request) {
        authService.revokeSession(sessionId, principal, request);
        return ResponseEntity.ok(ApiResponse.success("ยกเลิก session สำเร็จ", null));
    }

    @DeleteMapping
    ResponseEntity<ApiResponse<Void>> revokeAllSessions(
            @AuthenticationPrincipal JwtPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response) {
        authService.revokeAllSessions(principal, request, response, null);
        return ResponseEntity.ok(ApiResponse.success("ยกเลิก session ทั้งหมดสำเร็จ", null));
    }
}
