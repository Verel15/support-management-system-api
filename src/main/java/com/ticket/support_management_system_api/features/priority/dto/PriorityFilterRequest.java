package com.ticket.support_management_system_api.features.priority.dto;

import com.ticket.support_management_system_api.features.priority.enums.EDateRangeFilter;
import lombok.Data;

@Data
public class PriorityFilterRequest {
    private String keyword;
    private EDateRangeFilter dateRange;
}
