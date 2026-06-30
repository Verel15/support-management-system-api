package com.ticket.support_management_system_api.features.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TicketSummaryResponse {
    private long avgOpen;
    private long avgOverdue;
    private long avgSuccess;
    private String openTrend;
    private String overdueTrend;
    private String successTrend;
}
