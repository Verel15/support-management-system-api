package com.ticket.support_management_system_api.features.report.repository;

import com.ticket.support_management_system_api.features.report.entities.ReportExportCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ReportExportCounterRepository extends JpaRepository<ReportExportCounter, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ReportExportCounter> findByDayKey(String dayKey);
}
