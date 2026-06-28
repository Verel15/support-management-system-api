package com.ticket.support_management_system_api.features.upload.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileUploadResponse {
    private String fileName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
}
