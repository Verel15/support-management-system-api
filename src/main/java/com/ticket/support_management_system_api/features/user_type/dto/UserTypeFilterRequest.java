package com.ticket.support_management_system_api.features.user_type.dto;

import com.ticket.support_management_system_api.common.enums.EDateRangeFilter;
import lombok.Data;

@Data
public class UserTypeFilterRequest {
    private String keyword;
    private EDateRangeFilter dateRange;
}
