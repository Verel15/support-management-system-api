package com.ticket.support_management_system_api.features.project.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ProjectTicketStatsResponse {

    private UUID projectId;
    private long totalTickets;
    private List<StatusGroupCount> statusGroups;

    @Getter
    @Builder
    public static class StatusGroupCount {
        private String statusGroup;
        private long count;
    }
}
