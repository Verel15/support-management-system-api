package com.ticket.support_management_system_api.features.ticket.dto;

import com.ticket.support_management_system_api.features.status.enums.StatusGroup;
import com.ticket.support_management_system_api.features.ticket.enums.ERemainingTimeFilter;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketFilterRequest {

    private UUID projectId;
    private UUID statusId;
    private StatusGroup statusGroup;
    private UUID priorityId;
    private UUID statusFlowId;
    private String keyword;
    private Boolean overdue;
    private ERemainingTimeFilter remainingTime;
}
