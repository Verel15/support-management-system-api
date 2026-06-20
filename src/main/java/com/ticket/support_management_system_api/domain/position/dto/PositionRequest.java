package com.ticket.support_management_system_api.domain.position.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PositionRequest {

    @NotBlank(message = "ชื่อตำแหน่งต้องไม่ว่าง")
    @Size(max = 200, message = "ชื่อตำแหน่งต้องไม่เกิน 200 ตัวอักษร")
    private String name;
}
