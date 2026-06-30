package com.ticket.support_management_system_api.features.ticket.service;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.ticket.dto.AddAssigneeRequest;
import com.ticket.support_management_system_api.features.ticket.dto.TicketAssigneeResponse;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.entities.TicketAssignee;
import com.ticket.support_management_system_api.features.ticket.repository.TicketAssigneeRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketAssigneeService {

    private final TicketAssigneeRepository assigneeRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TicketAssigneeResponse> findAllByTicket(UUID ticketId) {
        getTicketOrThrow(ticketId);
        return assigneeRepository.findAllByTicketIdAndArchivedAtIsNull(ticketId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TicketAssigneeResponse addAssignee(UUID ticketId, AddAssigneeRequest request) {
        Ticket ticket = getTicketOrThrow(ticketId);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + request.getUserId()));

        TicketAssignee assignee = assigneeRepository.findByTicketIdAndUserId(ticketId, request.getUserId())
                .map(existing -> {
                    if (existing.getArchivedAt() == null) {
                        throw new DuplicateResourceException("ผู้ใช้นี้เป็นผู้รับผิดชอบ Ticket นี้อยู่แล้ว");
                    }
                    existing.setArchivedAt(null);
                    existing.setArchivedBy(null);
                    return existing;
                })
                .orElseGet(() -> TicketAssignee.builder().ticket(ticket).user(user).build());

        return toResponse(assigneeRepository.save(assignee));
    }

    public void removeAssignee(UUID ticketId, UUID userId, UUID actorUserId) {
        getTicketOrThrow(ticketId);
        TicketAssignee assignee = assigneeRepository.findByTicketIdAndUserIdAndArchivedAtIsNull(ticketId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้รับผิดชอบนี้ใน Ticket"));
        assignee.setArchivedAt(LocalDateTime.now());
        assignee.setArchivedBy(actorUserId);
        assigneeRepository.save(assignee);
    }

    private Ticket getTicketOrThrow(UUID ticketId) {
        return ticketRepository.findByIdAndArchivedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบ Ticket id: " + ticketId));
    }

    private TicketAssigneeResponse toResponse(TicketAssignee assignee) {
        User user = assignee.getUser();
        return TicketAssigneeResponse.builder()
                .id(assignee.getId())
                .userId(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .profileImageUrl(user.getProfileImageUrl())
                .assignedAt(assignee.getCreatedAt())
                .build();
    }
}
