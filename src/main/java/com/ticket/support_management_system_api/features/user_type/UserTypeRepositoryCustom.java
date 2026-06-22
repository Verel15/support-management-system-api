package com.ticket.support_management_system_api.features.user_type;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ticket.support_management_system_api.features.user_type.entities.UserType;

public interface UserTypeRepositoryCustom {

    Optional<UserType> findByName(String name);

    List<UserType> findAllOrderByNameAsc();

    List<UserType> findAllActiveOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);
}
