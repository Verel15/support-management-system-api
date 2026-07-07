package com.ticket.support_management_system_api.features.ticket.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SuggestedAssigneeUser {
    private UUID userId;
    private String fullName;
    private String profileImageUrl;
    private String positionName;
    private long openTicketCount;
}
