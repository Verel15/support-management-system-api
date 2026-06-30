package com.ticket.support_management_system_api.features.ticket.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketStatusLogResponse {
    private UUID id;
    private UUID changedById;
    private String changedByFullName;
    private String changedByProfileImageUrl;
    private UUID fromStatusId;
    private String fromStatusName;
    private UUID toStatusId;
    private String toStatusName;
    private String note;
    private LocalDateTime createdAt;
}
