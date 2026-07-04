package com.ticket.support_management_system_api.features.project.dto;

import com.ticket.support_management_system_api.features.project.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ProjectResponse {

    private UUID id;
    private String name;
    private String color;

    private UUID companyId;
    private String companyName;

    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;

    private long totalMembers;
    private long customerCount;
    private long assigneeCount;
    private long documentCount;

    private List<ProjectMemberSummaryResponse> members;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
