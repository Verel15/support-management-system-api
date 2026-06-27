package com.ticket.support_management_system_api.features.ticket_category.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCategoryRequest {

    @NotBlank(message = "ชื่อหมวดหมู่ต้องไม่ว่าง")
    @Size(max = 100, message = "ชื่อหมวดหมู่ต้องไม่เกิน 100 ตัวอักษร")
    private String name;

    @NotNull(message = "Status Flow ต้องไม่ว่าง")
    private UUID statusFlowId;

    @NotNull(message = "หมวดหมู่ย่อยต้องไม่ว่าง")
    @NotEmpty(message = "ต้องเลือกหมวดหมู่ย่อยอย่างน้อย 1 รายการ")
    private List<UUID> subCategoryIds;
}
