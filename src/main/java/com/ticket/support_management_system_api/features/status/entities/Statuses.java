package com.ticket.support_management_system_api.features.status.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "statuses", 
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "flow_id",
                "group_code",
                "sequence"
        })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Statuses extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id", nullable = false)
    private StatusFlows flow;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_code", nullable = false)
    private EStatusGroup group;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false)
    private Boolean isSystem;

}
