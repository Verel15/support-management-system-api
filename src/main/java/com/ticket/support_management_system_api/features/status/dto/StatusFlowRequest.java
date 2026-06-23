package com.ticket.support_management_system_api.features.status.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class StatusFlowRequest {

    @NotBlank(message = "ชื่อสถานะต้องไม่ว่าง")
    @Size(max = 200, message = "ชื่อสถานะต้องไม่เกิน 200 ตัวอักษร")
    private String name;

    private List<@NotBlank(message = "ชื่อสถานะใน PROCESS ต้องไม่ว่าง") @Size(max = 200) String> processStatuses;
}
