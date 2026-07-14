package com.ticket.support_management_system_api.features.ticket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTicketDueDateRequest {

    @NotNull(message = "กรุณาระบุวันครบกำหนด")
    private LocalDateTime dueDate;
}
