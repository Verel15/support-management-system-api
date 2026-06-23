package com.ticket.support_management_system_api.features.auth.enums;

public enum AuditEvent {
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    ACCOUNT_LOCKED,
    LOGOUT,
    TOKEN_REFRESH,
    SESSION_REVOKED,
    ALL_SESSIONS_REVOKED,
    SUSPICIOUS_REUSE
}
