package com.ticket.support_management_system_api.features.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ticket.support_management_system_api.features.user.dto.UserFilterRequest;
import com.ticket.support_management_system_api.features.user.entities.User;


public interface UserRepositoryCustom {

    Page<User> findAllActive(UserFilterRequest filter, Pageable pageable);
}