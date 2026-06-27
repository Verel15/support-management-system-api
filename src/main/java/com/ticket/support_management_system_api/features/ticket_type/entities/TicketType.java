package com.ticket.support_management_system_api.features.ticket_type.entities;

import java.util.ArrayList;
import java.util.List;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.ticket_category.entities.TicketCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "ticket_types",
    indexes = {
        @Index(name = "idx_ticket_types_archived_at", columnList = "archived_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketType extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ticket_type_categories",
        joinColumns = @JoinColumn(name = "type_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<TicketCategory> categories = new ArrayList<>();
}
