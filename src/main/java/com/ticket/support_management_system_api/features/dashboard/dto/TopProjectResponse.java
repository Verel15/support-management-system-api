package com.ticket.support_management_system_api.features.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TopProjectResponse {
    private List<ProjectTicketCount> projects;

    @Getter
    @Builder
    public static class ProjectTicketCount {
        private String projectName;
        private long ticketCount;
    }
}
