package com.ticket.support_management_system_api.features.project.dto;

import com.ticket.support_management_system_api.features.project.enums.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ProjectMemberRequest {

    @NotNull(message = "ผู้ใช้ต้องไม่ว่าง")
    private UUID userId;

    @NotNull(message = "บทบาทต้องไม่ว่าง")
    private ProjectMemberRole role;
}
