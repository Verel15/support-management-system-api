package com.ticket.support_management_system_api.features.ticket_sub_category.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketSubCategoryRequest {

    @NotBlank(message = "ชื่อหมวดหมู่ย่อยต้องไม่ว่าง")
    @Size(max = 100, message = "ชื่อหมวดหมู่ย่อยต้องไม่เกิน 100 ตัวอักษร")
    private String name;

    @NotNull(message = "ลำดับความสำคัญต้องไม่ว่าง")
    private UUID priorityLevelId;

    @NotNull(message = "ตำแหน่งที่เกี่ยวข้องต้องไม่ว่าง")
    private UUID positionId;
}
