package com.ticket.support_management_system_api.features.report.dto;

import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ReportTicketRow {
    private UUID id;
    private String ticketId;
    private String title;
    private UUID companyId;
    private String companyName;
    private UUID projectId;
    private String projectName;
    private List<UUID> assigneeIds;
    private String assigneesDisplay;
    private String priorityName;
    private String categoryName;
    private String currentStatusName;
    private EStatusGroup currentStatusGroup;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Double resolutionHours;
    private boolean overdue;
}
