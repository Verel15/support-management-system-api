package com.ticket.support_management_system_api.features.project.repository;

import com.ticket.support_management_system_api.features.project.entities.ProjectMember;
import com.ticket.support_management_system_api.features.project.enums.ProjectMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {
    @Query("SELECT m FROM ProjectMember m JOIN FETCH m.user WHERE m.project.id = :projectId AND m.archivedAt IS NULL")
    List<ProjectMember> findAllByProjectIdAndArchivedAtIsNull(@Param("projectId") UUID projectId);
    List<ProjectMember> findAllByProjectIdAndRoleAndArchivedAtIsNull(UUID projectId, ProjectMemberRole role);
    boolean existsByProjectIdAndUserIdAndRoleAndArchivedAtIsNull(UUID projectId, UUID userId, ProjectMemberRole role);
    Optional<ProjectMember> findByIdAndProjectIdAndArchivedAtIsNull(UUID id, UUID projectId);
    long countByProjectIdAndArchivedAtIsNull(UUID projectId);
    long countByProjectIdAndRoleAndArchivedAtIsNull(UUID projectId, ProjectMemberRole role);
}
