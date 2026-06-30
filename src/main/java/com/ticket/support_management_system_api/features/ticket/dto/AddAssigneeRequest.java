package com.ticket.support_management_system_api.features.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddAssigneeRequest {

    @NotNull(message = "กรุณาเลือกผู้รับผิดชอบ")
    private UUID userId;
}
