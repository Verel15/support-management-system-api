package com.ticket.support_management_system_api.features.report.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PdfExportContext {
    private String documentNo;
    private LocalDateTime exportedAt;
    private String exporterName;

    private String companyLabel;
    private String projectLabel;
    private String dateRangeLabel;
    private String assigneeLabel;
    private String priorityLabel;
    private String statusLabel;

    private long totalTickets;
    private long closedTickets;
    private long processingTickets;
    private long pendingTickets;
    private double avgResolutionHours;
    private double slaCompliancePercent;

    private List<CategoryCount> categoryCounts;

    @Data
    @Builder
    public static class CategoryCount {
        private String name;
        private long count;
    }
}
