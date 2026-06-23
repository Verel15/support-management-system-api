package com.ticket.support_management_system_api.features.status.dto;

import com.ticket.support_management_system_api.features.status.enums.StatusGroup;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StatusFlowResponse {

    private UUID id;
    private String name;
    private List<StatusItemResponse> statuses;
    private int ticketCount;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private String updatedByName;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class StatusItemResponse {
        private UUID id;
        private StatusGroup group;
        private String name;
        private Integer sequence;
        private Boolean isSystem;
    }
}
