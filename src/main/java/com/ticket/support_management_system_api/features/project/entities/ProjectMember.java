package com.ticket.support_management_system_api.features.project.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.project.enums.ProjectMemberRole;
import com.ticket.support_management_system_api.features.user.entities.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id", "role"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectMemberRole role;
}
