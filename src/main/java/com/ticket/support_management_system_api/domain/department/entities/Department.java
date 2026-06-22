package com.ticket.support_management_system_api.domain.department.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
