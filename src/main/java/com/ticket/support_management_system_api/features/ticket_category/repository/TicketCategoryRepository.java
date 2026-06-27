package com.ticket.support_management_system_api.features.ticket_category.repository;

import com.ticket.support_management_system_api.features.ticket_category.entities.TicketCategory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {

    @EntityGraph(attributePaths = {"statusFlow", "subCategories"})
    Page<TicketCategory> findAllByArchivedAtIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"statusFlow", "subCategories"})
    Optional<TicketCategory> findByIdAndArchivedAtIsNull(UUID id);

    @EntityGraph(attributePaths = {"statusFlow", "subCategories"})
    List<TicketCategory> findAllByIdInAndArchivedAtIsNull(List<UUID> ids);

    boolean existsByNameAndArchivedAtIsNull(String name);
    boolean existsByNameAndArchivedAtIsNullAndIdNot(String name, UUID id);
    boolean existsBySubCategoriesIdAndArchivedAtIsNull(UUID subCategoryId);
}
