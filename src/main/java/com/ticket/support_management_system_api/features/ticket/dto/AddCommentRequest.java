package com.ticket.support_management_system_api.features.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCommentRequest {

    @NotBlank(message = "เนื้อหาความคิดเห็นต้องไม่ว่าง")
    private String content;
}
