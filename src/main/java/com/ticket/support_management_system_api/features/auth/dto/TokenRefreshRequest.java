package com.ticket.support_management_system_api.features.auth.dto;

import lombok.Getter;

@Getter
public class TokenRefreshRequest {
    private String refreshToken;
}
