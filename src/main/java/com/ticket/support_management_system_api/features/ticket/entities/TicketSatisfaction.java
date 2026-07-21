package com.ticket.support_management_system_api.features.ticket.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "ticket_satisfactions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_ticket_satisfactions_ticket_id",
        columnNames = {"ticket_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSatisfaction extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @Column(nullable = false)
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "rated_at", nullable = false)
    private LocalDateTime ratedAt;
}
