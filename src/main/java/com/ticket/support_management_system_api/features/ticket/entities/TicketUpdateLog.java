package com.ticket.support_management_system_api.features.ticket.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.user.entities.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "ticket_update_logs",
    indexes = {
        @Index(name = "idx_ticket_update_logs_ticket_id", columnList = "ticket_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketUpdateLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
}
