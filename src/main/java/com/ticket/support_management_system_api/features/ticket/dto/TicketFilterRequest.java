package com.ticket.support_management_system_api.features.ticket.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TicketFilterRequest {

    private UUID projectId;
    private UUID statusId;
    private UUID priorityId;
    private UUID statusFlowId;
    private String keyword;
    private Boolean overdue;
}
