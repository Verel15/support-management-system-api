package com.ticket.support_management_system_api.features.ticket.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.user.entities.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "ticket_assignees",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_ticket_assignees_ticket_user",
        columnNames = {"ticket_id", "user_id"}
    ),
    indexes = {
        @Index(name = "idx_ticket_assignees_ticket_id", columnList = "ticket_id"),
        @Index(name = "idx_ticket_assignees_archived_at", columnList = "archived_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAssignee extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
