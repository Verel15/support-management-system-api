package com.ticket.support_management_system_api.domain.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "ชื่อแผนกต้องไม่ว่าง")
    @Size(max = 200, message = "ชื่อแผนกต้องไม่เกิน 200 ตัวอักษร")
    private String name;
}
