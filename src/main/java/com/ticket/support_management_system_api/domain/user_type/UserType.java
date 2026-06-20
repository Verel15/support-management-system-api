package com.ticket.support_management_system_api.domain.user_type;

import jakarta.persistence.*;
import lombok.*;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.domain.user_type.enums.MyTicketAccess;

@Entity
@Table(name = "user_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserType extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "my_ticket_access", nullable = false)
    private MyTicketAccess myTicketAccess;

    @Column(name = "all_project_access", nullable = false)
    private boolean allProjectAccess;

    @Column(name = "notification_access", nullable = false)
    private boolean notificationAccess;

    @Column(name = "dashboard_access", nullable = false)
    private boolean dashboardAccess;

    @Column(name = "all_ticket_access", nullable = false)
    private boolean allTicketAccess;

    @Column(name = "manage_project_access", nullable = false)
    private boolean manageProjectAccess;

    @Column(name = "manage_user_access", nullable = false)
    private boolean manageUserAccess;

    @Column(name = "manage_company_access", nullable = false)
    private boolean manageCompanyAccess;

    @Column(name = "manage_data_access", nullable = false)
    private boolean manageDataAccess;
}
