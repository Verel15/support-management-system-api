package com.ticket.support_management_system_api.features.user.dto;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.enums.CommonStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private AccountType accountType;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImageUrl;
    private CommonStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Customer details
    private UUID companyId;
    private String companyName;

    // Staff details
    private UUID userTypeId;
    private String userTypeName;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;
}
