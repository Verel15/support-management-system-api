package com.ticket.support_management_system_api.features.ticket.repository;

import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByIdAndArchivedAtIsNull(UUID id);

    Page<Ticket> findAllByArchivedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    long countByProjectIdAndArchivedAtIsNull(UUID projectId);

    long countByProjectIdAndCurrentStatus_GroupAndArchivedAtIsNull(UUID projectId, EStatusGroup group);

    @Query("""
            SELECT t.currentStatus.group AS statusGroup, COUNT(t) AS ticketCount
            FROM Ticket t
            WHERE t.project.id = :projectId AND t.archivedAt IS NULL
            GROUP BY t.currentStatus.group
            """)
    List<TicketStatusGroupCount> countByProjectIdGroupByStatusGroup(@Param("projectId") UUID projectId);

    interface TicketStatusGroupCount {
        EStatusGroup getStatusGroup();
        long getTicketCount();
    }

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.archivedAt IS NULL
              AND t.currentStatus.group NOT IN :closedGroups
              AND t.dueDate IS NOT NULL
              AND t.dueDate <= :threshold
              AND t.rebalanceSuggestedAt IS NULL
            """)
    List<Ticket> findDueSoonUnnotified(@Param("closedGroups") List<EStatusGroup> closedGroups,
                                        @Param("threshold") LocalDateTime threshold);
}
