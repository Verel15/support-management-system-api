package com.ticket.support_management_system_api.features.priority.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.priority.enums.EIntervalUnit;
import com.ticket.support_management_system_api.features.priority.enums.EPriorityColorKey;
import com.ticket.support_management_system_api.features.priority.enums.EPriorityIconKey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "priority_levels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriorityLevels extends BaseEntity {
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "icon_shape", nullable = false)
    @Enumerated(EnumType.STRING)
    private EPriorityIconKey iconShape;

    @Column(name = "icon_color", nullable = false)
    @Enumerated(EnumType.STRING)
    private EPriorityColorKey iconColor;

    @Column(name = "interval_value", nullable = false)
    private Integer intervalValue;
    
    @Column(name = "interval_unit", nullable = false)
    @Enumerated(EnumType.STRING)
    private EIntervalUnit intervalUnit;

}
