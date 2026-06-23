package com.ticket.support_management_system_api.features.auth.model;

import com.ticket.support_management_system_api.common.enums.AccountType;

import java.util.List;
import java.util.UUID;

public record JwtPrincipal(
        UUID userId,
        String email,
        AccountType accountType,
        UUID userTypeId,
        List<String> permissions
) {}
