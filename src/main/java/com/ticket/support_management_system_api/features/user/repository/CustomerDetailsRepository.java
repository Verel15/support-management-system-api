package com.ticket.support_management_system_api.features.user.repository;

import com.ticket.support_management_system_api.features.user.entities.CustomerDetails;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerDetailsRepository extends JpaRepository<CustomerDetails, UUID> {

    @EntityGraph(attributePaths = {"company"})
    List<CustomerDetails> findAllByUserIdIn(List<UUID> userIds);

    @EntityGraph(attributePaths = {"company"})
    Optional<CustomerDetails> findByUserId(UUID userId);

    long countByCompanyId(UUID companyId);
}
