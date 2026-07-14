package com.ticket.support_management_system_api.features.notification.repository;

import com.ticket.support_management_system_api.features.notification.entities.Notification;
import com.ticket.support_management_system_api.features.notification.enums.ENotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query(value = "SELECT n FROM Notification n LEFT JOIN FETCH n.actor WHERE n.recipient.id = :recipientId AND n.archivedAt IS NULL AND (:type IS NULL OR n.type = :type) ORDER BY n.createdAt DESC",
           countQuery = "SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.archivedAt IS NULL AND (:type IS NULL OR n.type = :type)")
    Page<Notification> findFeedByRecipientId(@Param("recipientId") UUID recipientId, @Param("type") ENotificationType type, Pageable pageable);

    long countByRecipientIdAndReadAtIsNullAndArchivedAtIsNull(UUID recipientId);

    List<Notification> findAllByRecipientIdAndReadAtIsNullAndArchivedAtIsNull(UUID recipientId);

    Optional<Notification> findByIdAndRecipientIdAndArchivedAtIsNull(UUID id, UUID recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.recipient.id = :recipientId AND n.readAt IS NULL AND n.archivedAt IS NULL")
    void markAllReadByRecipientId(@Param("recipientId") UUID recipientId, @Param("now") LocalDateTime now);
}
