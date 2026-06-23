package com.ticket.support_management_system_api.features.user_type.repository;

import com.ticket.support_management_system_api.features.user_type.entities.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTypeRepository extends JpaRepository<UserType, UUID> {
    Optional<UserType> findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    List<UserType> findAllByArchivedAtIsNullOrderByNameAsc();
}
