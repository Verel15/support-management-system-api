package com.ticket.support_management_system_api.features.ticket_sub_category.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.position.entities.Position;
import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "ticket_sub_categories",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_ticket_sub_categories_name",
            columnNames = {"name"}
        )
    },
    indexes = {
        @Index(name = "idx_ticket_sub_categories_priority_id", columnList = "priority_level_id"),
        @Index(name = "idx_ticket_sub_categories_position_id", columnList = "position_id"),
        @Index(name = "idx_ticket_sub_categories_archived_at", columnList = "archived_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSubCategory extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "priority_level_id", nullable = false)
    private PriorityLevels priorityLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;
}
