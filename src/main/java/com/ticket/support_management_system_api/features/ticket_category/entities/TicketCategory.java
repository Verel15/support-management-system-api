package com.ticket.support_management_system_api.features.ticket_category.entities;

import java.util.ArrayList;
import java.util.List;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.status.entities.StatusFlows;
import com.ticket.support_management_system_api.features.ticket_sub_category.entities.TicketSubCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
    name = "ticket_categories",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_ticket_categories_name",
            columnNames = {"name"}
        )
    },
    indexes = {
        @Index(name = "idx_ticket_categories_status_flow_id", columnList = "status_flow_id"),
        @Index(name = "idx_ticket_categories_archived_at",    columnList = "archived_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategory extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_flow_id", nullable = false)
    private StatusFlows statusFlow;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ticket_category_sub_categories",
        joinColumns = @JoinColumn(name = "category_id"),
        inverseJoinColumns = @JoinColumn(name = "sub_category_id")
    )
    private List<TicketSubCategory> subCategories = new ArrayList<>();
}
