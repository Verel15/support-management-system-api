package com.ticket.support_management_system_api.features.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ENotificationType {
    TICKET_CREATED(ENotificationCategory.TICKETS),
    TICKET_UPDATED(ENotificationCategory.TICKETS),
    TICKET_DELETED(ENotificationCategory.TICKETS),
    TICKET_STATUS_CHANGED(ENotificationCategory.TICKETS),
    TICKET_ASSIGNED(ENotificationCategory.MY_TICKETS),
    TICKET_UNASSIGNED(ENotificationCategory.MY_TICKETS),
    TICKET_COMMENT_ADDED(ENotificationCategory.MY_TICKETS),
    PROJECT_MEMBER_ADDED(ENotificationCategory.PROJECT),
    PROJECT_MEMBER_REMOVED(ENotificationCategory.PROJECT),
    PROJECT_UPDATED(ENotificationCategory.PROJECT);

    private final ENotificationCategory category;

    public String getCategoryLabel() {
        return category.getLabel();
    }
}
