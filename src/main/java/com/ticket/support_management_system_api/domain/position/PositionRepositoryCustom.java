package com.ticket.support_management_system_api.domain.position;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ticket.support_management_system_api.domain.position.entities.Position;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepositoryCustom {

    Optional<Position> findByName(String name);

    List<Position> findAllOrderByNameAsc();

    List<Position> findAllActiveOrderByNameAsc();

    Page<Position> findAllActive(Pageable pageable);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);
}
