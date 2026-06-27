package com.ticket.support_management_system_api.features.ticket_sub_category.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketSubCategoryResponse {
    private UUID id;
    private String name;
    private UUID priorityLevelId;
    private String priorityLevelName;
    private UUID positionId;
    private String positionName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
