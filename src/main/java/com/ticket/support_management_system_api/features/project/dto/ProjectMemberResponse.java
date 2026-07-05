package com.ticket.support_management_system_api.features.project.dto;

import com.ticket.support_management_system_api.features.project.enums.EProjectMemberRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProjectMemberResponse {

    private UUID id;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;
    private EProjectMemberRole role;
    private LocalDateTime createdAt;
}
