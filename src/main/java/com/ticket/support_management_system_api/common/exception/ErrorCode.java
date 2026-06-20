package com.ticket.support_management_system_api.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "Not found"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "Duplicate resource"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation error"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus status;
    private final String defaultMessage;
}
