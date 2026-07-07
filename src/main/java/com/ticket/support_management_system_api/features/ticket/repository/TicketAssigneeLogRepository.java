package com.ticket.support_management_system_api.features.ticket.repository;

import com.ticket.support_management_system_api.features.ticket.entities.TicketAssigneeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketAssigneeLogRepository extends JpaRepository<TicketAssigneeLog, UUID> {

    @Query("SELECT l FROM TicketAssigneeLog l JOIN FETCH l.changedBy JOIN FETCH l.assigneeUser WHERE l.ticket.id = :ticketId ORDER BY l.createdAt ASC")
    List<TicketAssigneeLog> findAllByTicketId(@Param("ticketId") UUID ticketId);
}
