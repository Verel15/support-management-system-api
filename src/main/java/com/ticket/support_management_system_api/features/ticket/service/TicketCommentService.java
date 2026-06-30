package com.ticket.support_management_system_api.features.ticket.service;

import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.ticket.dto.AddCommentRequest;
import com.ticket.support_management_system_api.features.ticket.dto.TicketCommentResponse;
import com.ticket.support_management_system_api.features.ticket.dto.TicketTimelineItem;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.entities.TicketComment;
import com.ticket.support_management_system_api.features.ticket.entities.TicketStatusLog;
import com.ticket.support_management_system_api.features.ticket.enums.TicketCommentType;
import com.ticket.support_management_system_api.features.ticket.repository.TicketCommentRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketStatusLogRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketCommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketStatusLogRepository statusLogRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

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
                .commentType(TicketCommentType.COMMENT)
                .build();
        return toResponse(commentRepository.save(comment));
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
                .type(TicketCommentType.COMMENT)
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
                .type(TicketCommentType.STATUS_CHANGE)
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
}
