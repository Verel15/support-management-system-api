package com.ticket.support_management_system_api.features.ticket.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketAssigneeResponse {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String profileImageUrl;
    private LocalDateTime assignedAt;
}
