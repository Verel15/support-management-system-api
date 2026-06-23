package com.ticket.support_management_system_api.features.user_type.repository;

import com.ticket.support_management_system_api.features.user_type.entities.UserType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserTypeRepository extends JpaRepository<UserType, UUID> {
    Optional<UserType> findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    Page<UserType> findAllByArchivedAtIsNullOrderByNameAsc(Pageable pageable);
    Page<UserType> findAllByArchivedAtIsNullAndNameContainingIgnoreCaseOrderByNameAsc(String name, Pageable pageable);
}
