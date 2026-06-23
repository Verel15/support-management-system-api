package com.ticket.support_management_system_api.features.auth.dto;

import com.ticket.support_management_system_api.common.enums.AccountType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private AccountType accountType;
}
