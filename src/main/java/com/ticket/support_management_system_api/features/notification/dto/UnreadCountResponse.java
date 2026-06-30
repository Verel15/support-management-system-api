package com.ticket.support_management_system_api.features.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnreadCountResponse {
    private long count;
}
