package com.ticket.support_management_system_api.features.user.dto;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.enums.EDateRangeFilter;
import lombok.Data;

import java.util.UUID;

@Data
public class UserFilterRequest {
    private AccountType accountType;
    private EDateRangeFilter dateRange;
    private String keyword;
    private UUID companyId;
    private UUID userTypeId;
}
