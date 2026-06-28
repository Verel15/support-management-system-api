package com.ticket.support_management_system_api.features.project.repository;

import com.ticket.support_management_system_api.features.project.entities.ProjectDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectDocumentRepository extends JpaRepository<ProjectDocument, UUID> {
    List<ProjectDocument> findAllByProjectIdAndArchivedAtIsNull(UUID projectId);
    long countByProjectIdAndArchivedAtIsNull(UUID projectId);
    Optional<ProjectDocument> findByIdAndProjectId(UUID id, UUID projectId);
}
