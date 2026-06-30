package com.ticket.support_management_system_api.features.ticket.repository;

import com.ticket.support_management_system_api.features.ticket.entities.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {

    @Query("SELECT c FROM TicketComment c JOIN FETCH c.author WHERE c.ticket.id = :ticketId AND c.archivedAt IS NULL ORDER BY c.createdAt ASC")
    List<TicketComment> findAllByTicketIdAndArchivedAtIsNull(@Param("ticketId") UUID ticketId);

    Optional<TicketComment> findByIdAndTicketIdAndArchivedAtIsNull(UUID id, UUID ticketId);
}
