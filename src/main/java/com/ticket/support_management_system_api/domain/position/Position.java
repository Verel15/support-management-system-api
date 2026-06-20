package com.ticket.support_management_system_api.domain.position;

import com.ticket.support_management_system_api.common.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position extends BaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
