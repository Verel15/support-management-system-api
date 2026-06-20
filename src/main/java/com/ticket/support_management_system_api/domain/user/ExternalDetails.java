package com.ticket.support_management_system_api.domain.user;


import java.util.UUID;

import com.ticket.support_management_system_api.domain.department.Department;
import com.ticket.support_management_system_api.domain.position.Position;
import com.ticket.support_management_system_api.domain.user_type.UserType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "external_details ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ExternalDetails {
    @Id
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_type_id", nullable = false)
    private UserType userType;
}
