package com.ticket.support_management_system_api.features.user.repository;

import com.ticket.support_management_system_api.features.user.entities.StaffDetails;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StaffDetailsRepository extends JpaRepository<StaffDetails, UUID> {

    @EntityGraph(attributePaths = {"userType", "department", "position"})
    List<StaffDetails> findAllByUserIdIn(List<UUID> userIds);
}
