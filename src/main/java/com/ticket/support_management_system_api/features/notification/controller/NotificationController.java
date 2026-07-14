package com.ticket.support_management_system_api.features.notification.controller;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.notification.dto.NotificationResponse;
import com.ticket.support_management_system_api.features.notification.dto.UnreadCountResponse;
import com.ticket.support_management_system_api.features.notification.enums.ENotificationType;
import com.ticket.support_management_system_api.features.notification.service.NotificationService;
import com.ticket.support_management_system_api.features.notification.service.NotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSseService sseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ENotificationType type,
            @AuthenticationPrincipal JwtPrincipal user) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(notificationService.getFeed(user.userId(), type, pageable)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount(user.userId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal user) {
        notificationService.markAsRead(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("อ่านการแจ้งเตือนแล้ว", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal JwtPrincipal user) {
        notificationService.markAllAsRead(user.userId());
        return ResponseEntity.ok(ApiResponse.success("อ่านการแจ้งเตือนทั้งหมดแล้ว", null));
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal JwtPrincipal user) {
        return sseService.subscribe(user.userId());
    }
}
