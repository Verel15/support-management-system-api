package com.ticket.support_management_system_api.features.ticket.dto;

import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import com.ticket.support_management_system_api.features.ticket.enums.ERemainingTimeFilter;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TicketFilterRequest {

    private UUID projectId;
    private UUID statusId;
    private EStatusGroup statusGroup;
    private UUID priorityId;
    private UUID statusFlowId;
    private String keyword;
    private Boolean overdue;
    private ERemainingTimeFilter remainingTime;
    private Instant dateFrom;
    private Instant dateTo;
}
