package com.ticket.support_management_system_api.features.ticket.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TicketAssigneeSummary {
    private UUID id;
    private String fullName;
    private String profileImageUrl;
}
