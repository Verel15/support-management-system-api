package com.ticket.support_management_system_api.features.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TicketTrendResponse {
    private long totalOpen;
    private long totalSuccess;
    private long totalOverdue;
    private List<MonthlyData> monthly;

    @Getter
    @Builder
    public static class MonthlyData {
        private int month;
        private long openCount;
        private long successCount;
        private long overdueCount;
    }
}
