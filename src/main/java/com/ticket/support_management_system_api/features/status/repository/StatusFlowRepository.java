package com.ticket.support_management_system_api.features.status.repository;

import com.ticket.support_management_system_api.features.status.entities.StatusFlows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StatusFlowRepository extends JpaRepository<StatusFlows, UUID> {
    Page<StatusFlows> findAllByArchivedAtIsNull(Pageable pageable);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
}
