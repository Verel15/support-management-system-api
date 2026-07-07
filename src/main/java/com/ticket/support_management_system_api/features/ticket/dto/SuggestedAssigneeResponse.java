package com.ticket.support_management_system_api.features.ticket.dto;

import com.ticket.support_management_system_api.features.ticket.enums.ESuggestedAssigneeReason;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuggestedAssigneeResponse {
    private SuggestedAssigneeUser suggested;
    private ESuggestedAssigneeReason reason;
}
