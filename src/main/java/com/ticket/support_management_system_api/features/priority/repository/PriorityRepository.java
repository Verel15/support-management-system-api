package com.ticket.support_management_system_api.features.priority.repository;

import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PriorityRepository extends JpaRepository<PriorityLevels, UUID>, JpaSpecificationExecutor<PriorityLevels> {
    boolean existsByNameAndArchivedAtIsNull(String name);
    boolean existsByNameAndArchivedAtIsNullAndIdNot(String name, UUID id);
    java.util.Optional<PriorityLevels> findByIdAndArchivedAtIsNull(UUID id);
}
