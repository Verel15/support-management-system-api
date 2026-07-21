package com.ticket.support_management_system_api.features.ticket.repository;

import com.ticket.support_management_system_api.features.ticket.entities.TicketSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketSatisfactionRepository extends JpaRepository<TicketSatisfaction, UUID> {

    Optional<TicketSatisfaction> findByTicketId(UUID ticketId);
}
