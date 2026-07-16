package com.ticket.support_management_system_api.features.report.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportSummaryResponse {
    private long totalTickets;
    private long resolvedTickets;
    private long overdueTickets;
    private double avgResolutionHours;
    private double slaCompliancePercent;
}
