package com.ticket.support_management_system_api.features.ticket_type.repository;

import com.ticket.support_management_system_api.features.ticket_type.entities.TicketType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    Page<TicketType> findAllByArchivedAtIsNullOrderByCreatedAt(Pageable pageable);

    Optional<TicketType> findByIdAndArchivedAtIsNull(UUID id);

    @EntityGraph(attributePaths = {
        "categories", "categories.statusFlow",
        "categories.subCategories", "categories.subCategories.priorityLevel",
        "categories.subCategories.position"
    })
    List<TicketType> findAllByArchivedAtIsNull();

    boolean existsByNameAndArchivedAtIsNull(String name);
    boolean existsByNameAndArchivedAtIsNullAndIdNot(String name, UUID id);
    boolean existsByCategoriesIdAndArchivedAtIsNull(UUID categoryId);
}
