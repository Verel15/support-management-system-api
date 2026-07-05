package com.ticket.support_management_system_api.features.user_type.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ticket.support_management_system_api.features.user_type.enums.EMyTicketAccess;

@Data
@Builder
public class UserTypeResponse {
    private UUID id;
    private String name;
    private EMyTicketAccess myTicketAccess;
    private boolean allProjectAccess;
    private boolean notificationAccess;
    private boolean dashboardAccess;
    private boolean allTicketAccess;
    private boolean manageProjectAccess;
    private boolean manageUserAccess;
    private boolean manageCompanyAccess;
    private boolean manageDataAccess;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
