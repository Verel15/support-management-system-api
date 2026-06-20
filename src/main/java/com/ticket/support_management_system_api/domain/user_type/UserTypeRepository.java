package com.ticket.support_management_system_api.domain.user_type;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserTypeRepository extends JpaRepository<UserType, UUID>, UserTypeRepositoryCustom {
}
