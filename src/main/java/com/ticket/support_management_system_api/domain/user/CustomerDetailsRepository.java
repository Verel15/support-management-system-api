package com.ticket.support_management_system_api.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerDetailsRepository extends JpaRepository<CustomerDetails, UUID> {
}
