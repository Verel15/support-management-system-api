package com.ticket.support_management_system_api.features.status.dto;

import com.ticket.support_management_system_api.common.enums.EDateRangeFilter;
import lombok.Data;

@Data
public class StatusFlowFilterRequest {
    private String keyword;
    private EDateRangeFilter dateRange;
}
