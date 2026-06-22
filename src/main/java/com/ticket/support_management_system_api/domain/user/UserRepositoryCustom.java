package com.ticket.support_management_system_api.domain.user;

import com.ticket.support_management_system_api.domain.user.dto.UserFilterRequest;
import com.ticket.support_management_system_api.domain.user.entities.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserRepositoryCustom {

    Page<User> findAllActive(UserFilterRequest filter, Pageable pageable);
}