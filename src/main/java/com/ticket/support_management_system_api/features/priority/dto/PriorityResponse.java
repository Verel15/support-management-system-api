package com.ticket.support_management_system_api.features.priority.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ticket.support_management_system_api.features.priority.enums.EIntervalUnit;
import com.ticket.support_management_system_api.features.priority.enums.EPriorityColorKey;
import com.ticket.support_management_system_api.features.priority.enums.EPriorityIconKey;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriorityResponse {
    private UUID id;
    private String name;
    private String description;
    private EPriorityIconKey iconShape;
    private EPriorityColorKey iconColor;
    private int intervalValue;
    private EIntervalUnit intervalUnit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
