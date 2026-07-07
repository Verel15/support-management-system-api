package com.ticket.support_management_system_api.features.notification.service;

import com.ticket.support_management_system_api.features.notification.enums.ENotificationType;
import com.ticket.support_management_system_api.features.ticket.repository.TicketAssigneeRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectMemberRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final TicketAssigneeRepository ticketAssigneeRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishTicketEvent(ENotificationType type, UUID ticketId, UUID actorId,
                                   String title, String message, Map<String, Object> metadata) {
        List<UUID> recipients = resolveTicketRecipients(ticketId, actorId);
        for (UUID recipientId : recipients) {
            try {
                notificationService.createNotification(recipientId, actorId, type, "TICKET", ticketId, title, message, metadata);
            } catch (Exception e) {
                log.error("Failed to create notification for recipient {}: {}", recipientId, e.getMessage());
            }
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishDirectEvent(ENotificationType type, UUID recipientId, UUID relatedId,
                                   String title, String message, Map<String, Object> metadata) {
        try {
            notificationService.createNotification(recipientId, null, type, "TICKET", relatedId, title, message, metadata);
        } catch (Exception e) {
            log.error("Failed to create notification for recipient {}: {}", recipientId, e.getMessage());
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishCommentEvent(UUID ticketId, UUID actorId, String title, String message,
                                    Map<String, Object> metadata) {
        List<UUID> recipients = resolveTicketRecipients(ticketId, actorId);
        for (UUID recipientId : recipients) {
            try {
                notificationService.createNotification(recipientId, actorId, ENotificationType.TICKET_COMMENT_ADDED,
                        "TICKET", ticketId, title, message, metadata);
            } catch (Exception e) {
                log.error("Failed to create notification for recipient {}: {}", recipientId, e.getMessage());
            }
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishProjectEvent(ENotificationType type, UUID projectId, UUID actorId,
                                    String title, String message, Map<String, Object> metadata) {
        List<UUID> recipients = resolveProjectRecipients(projectId, actorId);
        for (UUID recipientId : recipients) {
            try {
                notificationService.createNotification(recipientId, actorId, type, "PROJECT", projectId, title, message, metadata);
            } catch (Exception e) {
                log.error("Failed to create notification for recipient {}: {}", recipientId, e.getMessage());
            }
        }
    }

    private List<UUID> resolveTicketRecipients(UUID ticketId, UUID actorId) {
        List<UUID> adminIds = userRepository.findAdminUserIds();

        List<UUID> relatedIds = new ArrayList<>();
        ticketRepository.findById(ticketId).ifPresent(ticket ->
                relatedIds.add(ticket.getRequester().getId())
        );
        ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(ticketId)
                .forEach(a -> relatedIds.add(a.getUser().getId()));

        return mergeAndExcludeActor(adminIds, relatedIds, actorId);
    }

    private List<UUID> resolveProjectRecipients(UUID projectId, UUID actorId) {
        List<UUID> adminIds = userRepository.findAdminUserIds();

        List<UUID> memberIds = projectMemberRepository.findAllByProjectIdAndArchivedAtIsNull(projectId)
                .stream()
                .map(m -> m.getUser().getId())
                .collect(Collectors.toList());

        return mergeAndExcludeActor(adminIds, memberIds, actorId);
    }

    private List<UUID> mergeAndExcludeActor(List<UUID> adminIds, List<UUID> relatedIds, UUID actorId) {
        List<UUID> merged = new ArrayList<>(adminIds);
        for (UUID id : relatedIds) {
            if (!merged.contains(id)) {
                merged.add(id);
            }
        }
        merged.remove(actorId);
        return merged;
    }
}
