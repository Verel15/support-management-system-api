package com.ticket.support_management_system_api.features.status.repository;

import com.ticket.support_management_system_api.features.status.entities.StatusFlows;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface StatusFlowRepository extends JpaRepository<StatusFlows, UUID>, JpaSpecificationExecutor<StatusFlows> {
    Optional<StatusFlows> findByIdAndArchivedAtIsNull(UUID id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
}
