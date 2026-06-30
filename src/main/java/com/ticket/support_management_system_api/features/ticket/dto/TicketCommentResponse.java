package com.ticket.support_management_system_api.features.ticket.dto;

import com.ticket.support_management_system_api.features.ticket.enums.TicketCommentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketCommentResponse {
    private UUID id;
    private UUID authorId;
    private String authorFullName;
    private String authorProfileImageUrl;
    private String content;
    private TicketCommentType commentType;
    private LocalDateTime createdAt;
}
