package com.ticket.support_management_system_api.features.project.repository;

import com.ticket.support_management_system_api.features.project.entities.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findAllByArchivedAtIsNullOrderByCreatedAtDesc(Pageable pageable);
    Optional<Project> findByIdAndArchivedAtIsNull(UUID id);
    boolean existsByNameAndArchivedAtIsNull(String name);
    boolean existsByNameAndArchivedAtIsNullAndIdNot(String name, UUID id);

    long countByCompanyIdAndArchivedAtIsNull(UUID companyId);
}
