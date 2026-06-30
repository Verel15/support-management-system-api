package com.ticket.support_management_system_api.features.ticket.repository;

import com.ticket.support_management_system_api.features.ticket.entities.TicketStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketStatusLogRepository extends JpaRepository<TicketStatusLog, UUID> {

    @Query("SELECT l FROM TicketStatusLog l JOIN FETCH l.changedBy JOIN FETCH l.toStatus LEFT JOIN FETCH l.fromStatus WHERE l.ticket.id = :ticketId ORDER BY l.createdAt ASC")
    List<TicketStatusLog> findAllByTicketId(@Param("ticketId") UUID ticketId);
}
