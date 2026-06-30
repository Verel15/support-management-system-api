package com.ticket.support_management_system_api.features.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectStatusDistributionResponse {
    private long total;
    private long waitingCount;
    private long openCount;
    private long closeCount;
}
