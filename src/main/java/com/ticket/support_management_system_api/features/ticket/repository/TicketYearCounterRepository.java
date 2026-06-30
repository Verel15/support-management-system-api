package com.ticket.support_management_system_api.features.ticket.repository;

import com.ticket.support_management_system_api.features.ticket.entities.TicketYearCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TicketYearCounterRepository extends JpaRepository<TicketYearCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketYearCounter> findByYear(int year);
}
