package com.ticket.support_management_system_api.features.project.dto;

import com.ticket.support_management_system_api.common.enums.EDateRangeFilter;
import com.ticket.support_management_system_api.features.project.enums.ProjectStatus;
import lombok.Data;

@Data
public class ProjectFilterRequest {
    private String keyword;
    private EDateRangeFilter dateRange;
    private ProjectStatus status;
}
