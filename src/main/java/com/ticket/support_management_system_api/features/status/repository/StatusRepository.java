package com.ticket.support_management_system_api.features.status.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.ticket.support_management_system_api.features.status.entities.Statuses;

import java.util.List;
import java.util.UUID;

public interface StatusRepository extends JpaRepository<Statuses, UUID> {
    List<Statuses> findByFlowId(UUID flowId);
    List<Statuses> findAllByArchivedAtIsNotNullOrderByNameAsc();

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Statuses s WHERE s.flow.id = :flowId")
    void deleteByFlowId(UUID flowId);
}
