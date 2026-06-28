package com.ticket.support_management_system_api.features.project.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ProjectMemberSummaryResponse {

    private UUID id;
    private String fullName;
    private String profileImageUrl;
}
