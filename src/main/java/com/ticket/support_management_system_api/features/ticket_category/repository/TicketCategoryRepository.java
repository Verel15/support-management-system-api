package com.ticket.support_management_system_api.features.ticket_category.repository;

import com.ticket.support_management_system_api.features.ticket_category.entities.TicketCategory;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID>, JpaSpecificationExecutor<TicketCategory> {

    @EntityGraph(attributePaths = {"statusFlow", "subCategories"})
    Optional<TicketCategory> findByIdAndArchivedAtIsNull(UUID id);

    @EntityGraph(attributePaths = {"statusFlow", "subCategories"})
    List<TicketCategory> findAllByIdInAndArchivedAtIsNull(List<UUID> ids);

    boolean existsByNameAndArchivedAtIsNull(String name);
    boolean existsByNameAndArchivedAtIsNullAndIdNot(String name, UUID id);
    boolean existsBySubCategoriesIdAndArchivedAtIsNull(UUID subCategoryId);
}
