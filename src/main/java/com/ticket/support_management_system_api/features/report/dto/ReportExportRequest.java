package com.ticket.support_management_system_api.features.report.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReportExportRequest {

    @Valid
    @NotNull
    private ReportFilterRequest filter;

    private List<UUID> companyIds;

    private List<UUID> projectIds;

    @NotEmpty
    private List<ReportField> fields;

    @NotNull
    private ReportExportFormat format;
}
