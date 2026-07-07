package com.ticket.support_management_system_api.features.ticket.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.ticket.enums.ETicketAssigneeAction;
import com.ticket.support_management_system_api.features.user.entities.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "ticket_assignee_logs",
    indexes = {
        @Index(name = "idx_ticket_assignee_logs_ticket_id", columnList = "ticket_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAssigneeLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id", nullable = false)
    private User assigneeUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ETicketAssigneeAction action;
}
