package com.ticket.support_management_system_api.features.report.dto;

import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ReportFilterRequest {

    private Instant dateFrom;
    private Instant dateTo;
    private List<UUID> companyIds;
    private List<UUID> projectIds;
    private UUID priorityId;
    private EStatusGroup statusGroup;
    private UUID assigneeId;
}
