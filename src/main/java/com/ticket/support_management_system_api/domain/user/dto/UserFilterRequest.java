package com.ticket.support_management_system_api.domain.user.dto;

import com.ticket.support_management_system_api.common.enums.AccountType;
import lombok.Data;

@Data
public class UserFilterRequest {
    private AccountType accountType;
    private Integer createdWithinDays;
    private String keyword;
}
