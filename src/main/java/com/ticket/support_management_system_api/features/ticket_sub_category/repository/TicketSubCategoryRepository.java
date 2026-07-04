package com.ticket.support_management_system_api.features.ticket_sub_category.repository;

import com.ticket.support_management_system_api.features.ticket_sub_category.entities.TicketSubCategory;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketSubCategoryRepository extends JpaRepository<TicketSubCategory, UUID>, JpaSpecificationExecutor<TicketSubCategory> {

    @EntityGraph(attributePaths = {"priorityLevel", "position", "category", "category.statusFlow"})
    Optional<TicketSubCategory> findByIdAndArchivedAtIsNull(UUID id);

    @EntityGraph(attributePaths = {"priorityLevel", "position", "category", "category.statusFlow"})
    List<TicketSubCategory> findAllByIdInAndArchivedAtIsNull(List<UUID> ids);

    boolean existsByNameAndArchivedAtIsNull(String name);
    boolean existsByNameAndArchivedAtIsNullAndIdNot(String name, UUID id);
}
