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

    @Query("""
            SELECT l.ticket.id AS ticketId, MAX(l.createdAt) AS closedAt
            FROM TicketStatusLog l
            WHERE l.ticket.id IN :ticketIds
              AND l.toStatus.group IN (
                  com.ticket.support_management_system_api.features.status.enums.EStatusGroup.SUCCESS,
                  com.ticket.support_management_system_api.features.status.enums.EStatusGroup.FAILED
              )
            GROUP BY l.ticket.id
            """)
    List<TicketClosedAt> findLastClosedAtByTicketIdIn(@Param("ticketIds") List<UUID> ticketIds);

    interface TicketClosedAt {
        UUID getTicketId();
        java.time.LocalDateTime getClosedAt();
    }
}
