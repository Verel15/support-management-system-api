package com.ticket.support_management_system_api.features.ticket_sub_category.dto;

import com.ticket.support_management_system_api.common.enums.EDateRangeFilter;
import lombok.Data;

@Data
public class TicketSubCategoryFilterRequest {
    private String keyword;
    private EDateRangeFilter dateRange;
}
