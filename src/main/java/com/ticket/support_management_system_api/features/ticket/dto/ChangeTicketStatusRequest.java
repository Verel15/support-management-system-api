package com.ticket.support_management_system_api.features.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ChangeTicketStatusRequest {

    @NotNull(message = "กรุณาเลือกสถานะ")
    private UUID toStatusId;

    private String note;
}
