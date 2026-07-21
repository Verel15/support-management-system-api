package com.ticket.support_management_system_api.features.ticket.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketSatisfactionResponse {
    private Integer score;
    private String comment;
    private LocalDateTime ratedAt;
}
