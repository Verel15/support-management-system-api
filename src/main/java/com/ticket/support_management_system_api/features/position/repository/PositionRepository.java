package com.ticket.support_management_system_api.features.position.repository;

import com.ticket.support_management_system_api.features.position.entities.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    List<Position> findAllByArchivedAtIsNullOrderByNameAsc();
    Optional<Position> findByIdAndArchivedAtIsNull(UUID id);
}
