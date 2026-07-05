package com.ticket.support_management_system_api.features.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ENotificationCategory {
    TICKETS("Tickets ทั้งหมด"),
    MY_TICKETS("Tickets ของฉัน"),
    PROJECT("จัดการโครงการ");

    private final String label;
}
