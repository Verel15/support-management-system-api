package com.ticket.support_management_system_api.features.ticket.dto;

import com.ticket.support_management_system_api.features.ticket.enums.ETicketCommentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TicketTimelineItem {

    private UUID id;
    private ETicketCommentType type;
    private LocalDateTime createdAt;

    private UUID authorId;
    private String authorFullName;
    private String authorProfileImageUrl;

    private String content;

    private UUID fromStatusId;
    private String fromStatusName;
    private UUID toStatusId;
    private String toStatusName;
    private String note;

    private UUID assigneeUserId;
    private String assigneeFullName;
}
