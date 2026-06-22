package com.ticket.support_management_system_api.features.company.dto;

import com.ticket.support_management_system_api.common.enums.CommonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRequest {

    @NotBlank(message = "ชื่อบริษัทต้องไม่ว่าง")
    @Size(max = 200, message = "ชื่อบริษัทต้องไม่เกิน 200 ตัวอักษร")
    private String name;

    @Size(max = 500, message = "URL รูปโลโก้ต้องไม่เกิน 500 ตัวอักษร")
    private String logoImageUrl;

    @NotNull(message = "กรุณาระบุสถานะบริษัท")
    private CommonStatus status;
}
