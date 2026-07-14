package com.ticket.support_management_system_api.features.ticket.repository;

import com.ticket.support_management_system_api.features.ticket.entities.TicketUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketUpdateLogRepository extends JpaRepository<TicketUpdateLog, UUID> {

    @Query("SELECT l FROM TicketUpdateLog l JOIN FETCH l.changedBy WHERE l.ticket.id = :ticketId ORDER BY l.createdAt ASC")
    List<TicketUpdateLog> findAllByTicketId(@Param("ticketId") UUID ticketId);
}
