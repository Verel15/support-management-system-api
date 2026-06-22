package com.ticket.support_management_system_api.domain.position;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket.support_management_system_api.domain.position.entities.Position;

import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID>, PositionRepositoryCustom {
}
