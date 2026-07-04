package com.ticket.support_management_system_api.common.exception;

public class ReauthenticationFailedException extends RuntimeException {
    public ReauthenticationFailedException(String message) {
        super(message);
    }
}
