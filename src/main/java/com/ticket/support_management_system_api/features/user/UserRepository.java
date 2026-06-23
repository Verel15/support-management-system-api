package com.ticket.support_management_system_api.features.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket.support_management_system_api.features.user.entities.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryCustom {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<User> findByEmail(String email);
}
