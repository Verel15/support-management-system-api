package com.ticket.support_management_system_api.features.user.repository;

import com.ticket.support_management_system_api.features.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<User> findByEmail(String email);

    @Query("SELECT ed.user.id FROM ExternalDetails ed WHERE ed.userType.allTicketAccess = true AND ed.userType.allProjectAccess = true AND ed.user.archivedAt IS NULL")
    List<UUID> findAdminUserIds();
}
