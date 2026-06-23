package com.ticket.support_management_system_api.features.status.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ticket.support_management_system_api.features.status.enums.StatusGroup;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusResponse {
    private UUID id;
    private UUID flowId;
    private StatusGroup group;
    private String name;
    private Integer sequence;
    private Boolean isSystem;
    private LocalDateTime archivedAt;
    private UUID archivedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
