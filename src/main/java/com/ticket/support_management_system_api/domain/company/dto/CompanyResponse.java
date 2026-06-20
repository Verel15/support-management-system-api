package com.ticket.support_management_system_api.domain.company.dto;

import com.ticket.support_management_system_api.common.enums.CommonStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CompanyResponse {

    private UUID id;
    private String name;
    private String logoImageUrl;
    private CommonStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
