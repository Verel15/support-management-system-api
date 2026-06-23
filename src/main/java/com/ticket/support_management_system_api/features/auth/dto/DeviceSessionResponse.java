package com.ticket.support_management_system_api.features.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class DeviceSessionResponse {

    private UUID id;
    private String deviceName;
    private String ipAddress;
    private LocalDateTime lastActiveAt;
    private LocalDateTime createdAt;
    private boolean current;
}
