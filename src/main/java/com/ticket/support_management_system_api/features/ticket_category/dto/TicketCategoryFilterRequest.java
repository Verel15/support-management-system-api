package com.ticket.support_management_system_api.features.ticket_category.dto;

import com.ticket.support_management_system_api.common.enums.EDateRangeFilter;
import lombok.Data;

@Data
public class TicketCategoryFilterRequest {
    private String keyword;
    private EDateRangeFilter dateRange;
}
