package com.ticket.support_management_system_api.features.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProjectRequest {

    @NotBlank(message = "ชื่อโครงการต้องไม่ว่าง")
    @Size(max = 100, message = "ชื่อโครงการต้องไม่เกิน 100 ตัวอักษร")
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "สีต้องอยู่ในรูปแบบ hex เช่น #FF5733")
    private String color;

    @NotNull(message = "บริษัทต้องไม่ว่าง")
    private UUID companyId;

    @NotNull(message = "วันเริ่มต้นโครงการต้องไม่ว่าง")
    private LocalDate startDate;

    @NotNull(message = "วันสิ้นสุดโครงการต้องไม่ว่าง")
    private LocalDate endDate;
}
