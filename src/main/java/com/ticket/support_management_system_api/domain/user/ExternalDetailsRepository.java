package com.ticket.support_management_system_api.domain.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket.support_management_system_api.domain.user.entities.ExternalDetails;

import java.util.List;
import java.util.UUID;

public interface ExternalDetailsRepository extends JpaRepository<ExternalDetails, UUID> {

    @EntityGraph(attributePaths = {"userType", "department", "position"})
    List<ExternalDetails> findAllByUserIdIn(List<UUID> userIds);
}
