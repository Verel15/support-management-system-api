package com.ticket.support_management_system_api.features.notification.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.notification.enums.ENotificationType;
import com.ticket.support_management_system_api.features.user.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient_id", columnList = "recipient_id"),
                @Index(name = "idx_notifications_recipient_created", columnList = "recipient_id, created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ENotificationType type;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;
}
