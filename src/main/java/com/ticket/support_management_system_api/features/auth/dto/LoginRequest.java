package com.ticket.support_management_system_api.features.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotBlank(message = "กรุณากรอกอีเมล")
    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    @Schema(example = "admin@example.com")
    private String email;

    @NotBlank(message = "กรุณากรอกรหัสผ่าน")
    @Schema(example = "Admin@1234")
    private String password;
}
