package com.ticket.support_management_system_api.features.notification.dto;

import com.ticket.support_management_system_api.features.notification.enums.NotificationCategory;
import com.ticket.support_management_system_api.features.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private NotificationType type;
    private NotificationCategory category;
    private String entityType;
    private UUID entityId;
    private String title;
    private String message;
    private boolean read;
    private UUID actorId;
    private String actorFullName;
    private String actorProfileImageUrl;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
