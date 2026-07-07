package com.ticket.support_management_system_api.features.ticket.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RebalanceSuggestionResponse {
    private boolean suggested;
    private String reason;
    private SuggestedAssigneeUser overloadedAssignee;
    private SuggestedAssigneeUser suggestedTransferTo;
}
