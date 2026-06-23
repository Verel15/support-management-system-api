package com.ticket.support_management_system_api.features.auth.repository;

import com.ticket.support_management_system_api.features.auth.entities.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, UUID> {

    List<DeviceSession> findByUserIdAndActiveTrue(UUID userId);

    @Modifying
    @Query("UPDATE DeviceSession d SET d.active = false WHERE d.user.id = :userId AND d.active = true AND d.id <> :exceptId")
    void deactivateAllExcept(UUID userId, UUID exceptId);

    @Modifying
    @Query("UPDATE DeviceSession d SET d.active = false WHERE d.user.id = :userId AND d.active = true")
    void deactivateAll(UUID userId);
}
