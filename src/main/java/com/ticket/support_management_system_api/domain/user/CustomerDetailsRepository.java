package com.ticket.support_management_system_api.domain.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket.support_management_system_api.domain.user.entities.CustomerDetails;

import java.util.List;
import java.util.UUID;

public interface CustomerDetailsRepository extends JpaRepository<CustomerDetails, UUID> {

    @EntityGraph(attributePaths = {"company"})
    List<CustomerDetails> findAllByUserIdIn(List<UUID> userIds);
}
