package com.ticket.support_management_system_api.features.ticket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateTicketRequest {

    @Size(max = 255, message = "หัวข้อ Ticket ต้องไม่เกิน 255 ตัวอักษร")
    private String title;

    @NotNull(message = "กรุณาเลือกโครงการ")
    private UUID projectId;

    @NotNull(message = "กรุณาเลือกประเภทย่อย Ticket")
    private UUID subCategoryId;

    private String description;
}
