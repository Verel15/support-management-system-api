package com.ticket.support_management_system_api.features.project.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProjectDocumentResponse {
    private UUID id;
    private String fileName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
