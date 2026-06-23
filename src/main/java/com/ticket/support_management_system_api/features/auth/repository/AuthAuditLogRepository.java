package com.ticket.support_management_system_api.features.auth.repository;

import com.ticket.support_management_system_api.features.auth.entities.AuthAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, UUID> {
}
