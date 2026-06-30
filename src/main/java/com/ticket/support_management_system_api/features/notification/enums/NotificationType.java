package com.ticket.support_management_system_api.features.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    TICKET_CREATED(NotificationCategory.TICKETS),
    TICKET_UPDATED(NotificationCategory.TICKETS),
    TICKET_DELETED(NotificationCategory.TICKETS),
    TICKET_STATUS_CHANGED(NotificationCategory.TICKETS),
    TICKET_ASSIGNED(NotificationCategory.MY_TICKETS),
    TICKET_UNASSIGNED(NotificationCategory.MY_TICKETS),
    TICKET_COMMENT_ADDED(NotificationCategory.MY_TICKETS),
    PROJECT_MEMBER_ADDED(NotificationCategory.PROJECT),
    PROJECT_MEMBER_REMOVED(NotificationCategory.PROJECT),
    PROJECT_UPDATED(NotificationCategory.PROJECT);

    private final NotificationCategory category;

    public String getCategoryLabel() {
        return category.getLabel();
    }
}
