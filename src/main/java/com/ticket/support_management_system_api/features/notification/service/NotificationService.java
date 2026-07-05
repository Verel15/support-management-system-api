package com.ticket.support_management_system_api.features.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.notification.dto.NotificationResponse;
import com.ticket.support_management_system_api.features.notification.dto.UnreadCountResponse;
import com.ticket.support_management_system_api.features.notification.entities.Notification;
import com.ticket.support_management_system_api.features.notification.enums.ENotificationType;
import com.ticket.support_management_system_api.features.notification.repository.NotificationRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationSseService sseService;
    private final ObjectMapper objectMapper;

    public Notification createNotification(UUID recipientId, UUID actorId, ENotificationType type,
                                           String entityType, UUID entityId, String title, String message,
                                           Map<String, Object> metadata) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + recipientId));
        User actor = actorId != null ? userRepository.findById(actorId).orElse(null) : null;

        String metadataJson = null;
        if (metadata != null && !metadata.isEmpty()) {
            try {
                metadataJson = objectMapper.writeValueAsString(metadata);
            } catch (Exception e) {
                log.warn("Failed to serialize metadata: {}", e.getMessage());
            }
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .entityType(entityType)
                .entityId(entityId)
                .title(title)
                .message(message)
                .metadata(metadataJson)
                .build();
        notification = notificationRepository.save(notification);

        sseService.push(recipientId, toResponse(notification));
        return notification;
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getFeed(UUID userId, Pageable pageable) {
        return PaginationUtils.toPageResponse(
                notificationRepository.findFeedByRecipientId(userId, pageable),
                this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        return UnreadCountResponse.builder()
                .count(notificationRepository.countByRecipientIdAndReadAtIsNullAndArchivedAtIsNull(userId))
                .build();
    }

    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndRecipientIdAndArchivedAtIsNull(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบการแจ้งเตือน id: " + notificationId));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllReadByRecipientId(userId, LocalDateTime.now());
    }

    public NotificationResponse toResponse(Notification notification) {
        User actor = notification.getActor();
        Map<String, Object> metadata = null;
        if (notification.getMetadata() != null) {
            try {
                metadata = objectMapper.readValue(notification.getMetadata(), new TypeReference<>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize metadata for notification {}: {}", notification.getId(), e.getMessage());
            }
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .category(notification.getType().getCategory())
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.getReadAt() != null)
                .actorId(actor != null ? actor.getId() : null)
                .actorFullName(actor != null ? actor.getFirstName() + " " + actor.getLastName() : null)
                .actorProfileImageUrl(actor != null ? actor.getProfileImageUrl() : null)
                .metadata(metadata)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
