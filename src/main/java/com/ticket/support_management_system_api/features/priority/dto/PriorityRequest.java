package com.ticket.support_management_system_api.features.priority.dto;

import com.ticket.support_management_system_api.features.priority.enums.EIntervalUnit;
import com.ticket.support_management_system_api.features.priority.enums.EPriorityColorKey;
import com.ticket.support_management_system_api.features.priority.enums.EPriorityIconKey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PriorityRequest {
    
    @NotBlank(message = "ชื่อลำดับความสำคัญต้องไม่ว่าง")
    @Size(max = 50, message = "ชื่อลำดับความสำคัญต้องไม่เกิน 50 ตัวอักษร")
    private String name;

    @Size(max = 100, message = "รายละเอียดลำดับความสำคัญต้องไม่เกิน 100 ตัวอักษร")
    private String description;
    
    @NotNull(message = "รูปร่างไอคอนต้องไม่ว่าง")
    private EPriorityIconKey iconShape;

    @NotNull(message = "สีไอคอนต้องไม่ว่าง")
    private EPriorityColorKey iconColor;

    @NotNull(message = "ค่าช่วงต้องไม่ว่าง")
    private Integer intervalValue;

    @NotNull(message = "หน่วยช่วงต้องไม่ว่าง")
    private EIntervalUnit intervalUnit;
}
