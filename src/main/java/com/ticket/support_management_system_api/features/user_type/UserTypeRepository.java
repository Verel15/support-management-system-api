package com.ticket.support_management_system_api.features.user_type;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket.support_management_system_api.features.user_type.entities.UserType;

import java.util.Optional;
import java.util.UUID;

public interface UserTypeRepository extends JpaRepository<UserType, UUID>, UserTypeRepositoryCustom {
    Optional<UserType> findByName(String name);
}
