package com.ticket.support_management_system_api.features.user.dto;

import com.ticket.support_management_system_api.common.enums.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserRequest {

    @NotNull(message = "กรุณาระบุรูปแบบผู้ใช้")
    private AccountType accountType;

    @NotBlank(message = "กรุณากรอกชื่อ")
    private String firstName;

    @NotBlank(message = "กรุณากรอกนามสกุล")
    private String lastName;

    private String phone;

    @NotBlank(message = "กรุณากรอกอีเมล")
    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    private String email;

    private String profileImageUrl;

    // For CUSTOMER
    private UUID companyId;

    // For STAFF
    private UUID userTypeId;
    private UUID departmentId;
    private UUID positionId;
}
