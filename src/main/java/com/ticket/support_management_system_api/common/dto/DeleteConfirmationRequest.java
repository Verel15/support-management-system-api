package com.ticket.support_management_system_api.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteConfirmationRequest {

    @NotBlank(message = "กรุณายืนยันรหัสผ่านก่อนลบข้อมูล")
    private String password;
}
