package com.ticket.support_management_system_api.features.ticket.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.status.entities.StatusFlows;
import com.ticket.support_management_system_api.features.status.entities.Statuses;
import com.ticket.support_management_system_api.features.ticket_sub_category.entities.TicketSubCategory;
import com.ticket.support_management_system_api.features.user.entities.User;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "tickets",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_tickets_year_seq",
        columnNames = {"ticket_year", "ticket_seq"}
    ),
    indexes = {
        @Index(name = "idx_tickets_archived_at", columnList = "archived_at"),
        @Index(name = "idx_tickets_project_id", columnList = "project_id"),
        @Index(name = "idx_tickets_status_flow_id", columnList = "status_flow_id"),
        @Index(name = "idx_tickets_current_status_id", columnList = "current_status_id"),
        @Index(name = "idx_tickets_year_seq", columnList = "ticket_year, ticket_seq")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ticket_year", nullable = false)
    private Integer ticketYear;

    @Column(name = "ticket_seq", nullable = false)
    private Integer ticketSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private TicketSubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_status_id", nullable = false)
    private Statuses currentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_flow_id", nullable = false)
    private StatusFlows statusFlow;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "rebalance_suggested_at")
    private LocalDateTime rebalanceSuggestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
}
