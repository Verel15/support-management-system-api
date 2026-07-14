package com.ticket.support_management_system_api.features.ticket.service;

import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.notification.service.NotificationEventPublisher;
import com.ticket.support_management_system_api.features.ticket.dto.AddCommentRequest;
import com.ticket.support_management_system_api.features.ticket.dto.TicketCommentResponse;
import com.ticket.support_management_system_api.features.ticket.dto.TicketTimelineItem;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.entities.TicketAssigneeLog;
import com.ticket.support_management_system_api.features.ticket.entities.TicketComment;
import com.ticket.support_management_system_api.features.ticket.entities.TicketStatusLog;
import com.ticket.support_management_system_api.features.ticket.entities.TicketUpdateLog;
import com.ticket.support_management_system_api.features.ticket.enums.ETicketAssigneeAction;
import com.ticket.support_management_system_api.features.ticket.enums.ETicketCommentType;
import com.ticket.support_management_system_api.features.ticket.repository.TicketAssigneeLogRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketCommentRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketStatusLogRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketUpdateLogRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketCommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketStatusLogRepository statusLogRepository;
    private final TicketAssigneeLogRepository assigneeLogRepository;
    private final TicketUpdateLogRepository updateLogRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Transactional(readOnly = true)
    public List<TicketCommentResponse> findAllByTicket(UUID ticketId) {
        getTicketOrThrow(ticketId);
        return commentRepository.findAllByTicketIdAndArchivedAtIsNull(ticketId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketTimelineItem> getTimeline(UUID ticketId) {
        getTicketOrThrow(ticketId);

        List<TicketTimelineItem> items = new ArrayList<>();

        commentRepository.findAllByTicketIdAndArchivedAtIsNull(ticketId)
                .forEach(c -> items.add(toTimelineComment(c)));

        statusLogRepository.findAllByTicketId(ticketId)
                .forEach(l -> items.add(toTimelineStatusLog(l)));

        assigneeLogRepository.findAllByTicketId(ticketId)
                .forEach(l -> items.add(toTimelineAssigneeLog(l)));

        updateLogRepository.findAllByTicketId(ticketId)
                .forEach(l -> items.add(toTimelineUpdateLog(l)));

        items.sort(Comparator.comparing(TicketTimelineItem::getCreatedAt));
        return items;
    }

    public TicketCommentResponse addComment(UUID ticketId, AddCommentRequest request, UUID actorUserId) {
        Ticket ticket = getTicketOrThrow(ticketId);
        User author = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + actorUserId));

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .author(author)
                .content(request.getContent())
                .commentType(ETicketCommentType.COMMENT)
                .build();
        TicketComment saved = commentRepository.save(comment);

        notificationEventPublisher.publishCommentEvent(
                ticketId, actorUserId,
                "Comment ใหม่ใน Ticket: " + ticket.getTitle(),
                author.getFirstName() + " " + author.getLastName() + " แสดงความคิดเห็น",
                Map.of(
                        "ticketTitle", ticket.getTitle(),
                        "commentPreview", request.getContent().length() > 80
                                ? request.getContent().substring(0, 80) + "..." : request.getContent()
                ));

        return toResponse(saved);
    }

    private Ticket getTicketOrThrow(UUID ticketId) {
        return ticketRepository.findByIdAndArchivedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบ Ticket id: " + ticketId));
    }

    private TicketCommentResponse toResponse(TicketComment comment) {
        User author = comment.getAuthor();
        return TicketCommentResponse.builder()
                .id(comment.getId())
                .authorId(author.getId())
                .authorFullName(author.getFirstName() + " " + author.getLastName())
                .authorProfileImageUrl(author.getProfileImageUrl())
                .content(comment.getContent())
                .commentType(comment.getCommentType())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private TicketTimelineItem toTimelineComment(TicketComment comment) {
        User author = comment.getAuthor();
        return TicketTimelineItem.builder()
                .id(comment.getId())
                .type(ETicketCommentType.COMMENT)
                .createdAt(comment.getCreatedAt())
                .authorId(author.getId())
                .authorFullName(author.getFirstName() + " " + author.getLastName())
                .authorProfileImageUrl(author.getProfileImageUrl())
                .content(comment.getContent())
                .build();
    }

    private TicketTimelineItem toTimelineStatusLog(TicketStatusLog log) {
        User changedBy = log.getChangedBy();
        return TicketTimelineItem.builder()
                .id(log.getId())
                .type(ETicketCommentType.STATUS_CHANGE)
                .createdAt(log.getCreatedAt())
                .authorId(changedBy.getId())
                .authorFullName(changedBy.getFirstName() + " " + changedBy.getLastName())
                .authorProfileImageUrl(changedBy.getProfileImageUrl())
                .fromStatusId(log.getFromStatus() != null ? log.getFromStatus().getId() : null)
                .fromStatusName(log.getFromStatus() != null ? log.getFromStatus().getName() : null)
                .toStatusId(log.getToStatus().getId())
                .toStatusName(log.getToStatus().getName())
                .note(log.getNote())
                .build();
    }

    private TicketTimelineItem toTimelineAssigneeLog(TicketAssigneeLog log) {
        User changedBy = log.getChangedBy();
        User assigneeUser = log.getAssigneeUser();
        return TicketTimelineItem.builder()
                .id(log.getId())
                .type(log.getAction() == ETicketAssigneeAction.ADDED
                        ? ETicketCommentType.ASSIGNEE_ADDED : ETicketCommentType.ASSIGNEE_REMOVED)
                .createdAt(log.getCreatedAt())
                .authorId(changedBy.getId())
                .authorFullName(changedBy.getFirstName() + " " + changedBy.getLastName())
                .authorProfileImageUrl(changedBy.getProfileImageUrl())
                .assigneeUserId(assigneeUser.getId())
                .assigneeFullName(assigneeUser.getFirstName() + " " + assigneeUser.getLastName())
                .build();
    }

    private TicketTimelineItem toTimelineUpdateLog(TicketUpdateLog log) {
        User changedBy = log.getChangedBy();
        return TicketTimelineItem.builder()
                .id(log.getId())
                .type(ETicketCommentType.FIELD_UPDATED)
                .createdAt(log.getCreatedAt())
                .authorId(changedBy.getId())
                .authorFullName(changedBy.getFirstName() + " " + changedBy.getLastName())
                .authorProfileImageUrl(changedBy.getProfileImageUrl())
                .fieldName(log.getFieldName())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .build();
    }
}
