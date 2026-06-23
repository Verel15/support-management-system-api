package com.ticket.support_management_system_api.features.status.entities;

import java.util.ArrayList;
import java.util.List;

import com.ticket.support_management_system_api.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "status_flows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusFlows extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "flow", fetch = FetchType.LAZY)
    private List<Statuses> statuses = new ArrayList<>();
}
