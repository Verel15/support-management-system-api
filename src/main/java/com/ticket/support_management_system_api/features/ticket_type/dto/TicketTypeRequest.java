package com.ticket.support_management_system_api.features.ticket_type.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketTypeRequest {

    @NotBlank(message = "ชื่อประเภทตั๋วต้องไม่ว่าง")
    @Size(max = 100, message = "ชื่อประเภทตั๋วต้องไม่เกิน 100 ตัวอักษร")
    private String name;

    @NotNull(message = "หมวดหมู่ต้องไม่ว่าง")
    @NotEmpty(message = "ต้องเลือกหมวดหมู่อย่างน้อย 1 รายการ")
    private List<UUID> categoryIds;
}
