package com.ticket.support_management_system_api.features.ticket.repository;

import com.ticket.support_management_system_api.features.ticket.entities.TicketAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketAssigneeRepository extends JpaRepository<TicketAssignee, UUID> {

    @Query("SELECT a FROM TicketAssignee a JOIN FETCH a.user WHERE a.ticket.id = :ticketId AND a.archivedAt IS NULL")
    List<TicketAssignee> findAllByTicketIdAndArchivedAtIsNull(@Param("ticketId") UUID ticketId);

    @Query("SELECT a FROM TicketAssignee a JOIN FETCH a.user WHERE a.ticket.id IN :ticketIds AND a.archivedAt IS NULL")
    List<TicketAssignee> findAllByTicketIdInAndArchivedAtIsNull(@Param("ticketIds") List<UUID> ticketIds);

    Optional<TicketAssignee> findByTicketIdAndUserIdAndArchivedAtIsNull(UUID ticketId, UUID userId);

    Optional<TicketAssignee> findByTicketIdAndUserId(UUID ticketId, UUID userId);

    boolean existsByTicketIdAndUserIdAndArchivedAtIsNull(UUID ticketId, UUID userId);
}
