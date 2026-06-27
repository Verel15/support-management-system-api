package com.ticket.support_management_system_api.features.priority.repository;

import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PriorityRepository extends JpaRepository<PriorityLevels, UUID> {
    boolean existsByNameAndArchivedAtIsNull(String name);
    boolean existsByNameAndArchivedAtIsNullAndIdNot(String name, UUID id);
    Page<PriorityLevels> findAllByArchivedAtIsNullOrderByCreatedAt(Pageable pageable);
    java.util.Optional<PriorityLevels> findByIdAndArchivedAtIsNull(UUID id);
}
