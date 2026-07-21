package com.ticket.support_management_system_api.features.ticket.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddSatisfactionRatingRequest {

    @NotNull(message = "กรุณาระบุคะแนนความพึงพอใจ")
    @Min(value = 1, message = "คะแนนต้องอยู่ระหว่าง 1-5")
    @Max(value = 5, message = "คะแนนต้องอยู่ระหว่าง 1-5")
    private Integer score;

    private String comment;
}
