package com.ticket.support_management_system_api.features.project.repository;

import com.ticket.support_management_system_api.features.project.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {
    Optional<Project> findByIdAndArchivedAtIsNull(UUID id);
    boolean existsByNameAndArchivedAtIsNull(String name);
    boolean existsByNameAndArchivedAtIsNullAndIdNot(String name, UUID id);

    long countByCompanyIdAndArchivedAtIsNull(UUID companyId);
    List<Project> findAllByCompanyIdAndArchivedAtIsNullOrderByCreatedAtDesc(UUID companyId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p WHERE p.id = :id AND p.archivedAt IS NULL AND (p.company.id = :companyId OR p.id IN :memberProjectIds)")
    boolean existsVisibleToCustomer(@Param("id") UUID id, @Param("companyId") UUID companyId, @Param("memberProjectIds") List<UUID> memberProjectIds);
}
