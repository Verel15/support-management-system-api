package com.ticket.support_management_system_api.features.user_type.service;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.user_type.dto.UserTypeRequest;
import com.ticket.support_management_system_api.features.user_type.dto.UserTypeResponse;
import com.ticket.support_management_system_api.features.user_type.entities.UserType;
import com.ticket.support_management_system_api.features.user_type.repository.UserTypeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserTypeService {

    private final UserTypeRepository userTypeRepository;

    @Transactional(readOnly = true)
    public List<UserTypeResponse> findAll() {
        return userTypeRepository.findAllByArchivedAtIsNullOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserTypeResponse findById(UUID id) {
        UserType userType = userTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบประเภทผู้ใช้ id: " + id));
        return toResponse(userType);
    }

    @Transactional
    public UserTypeResponse create(UserTypeRequest request) {
        if (userTypeRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("ชื่อประเภทผู้ใช้ '" + request.getName() + "' มีอยู่แล้ว");
        }
        UserType userType = UserType.builder()
                .name(request.getName())
                .myTicketAccess(request.getMyTicketAccess())
                .allProjectAccess(request.isAllProjectAccess())
                .notificationAccess(request.isNotificationAccess())
                .dashboardAccess(request.isDashboardAccess())
                .allTicketAccess(request.isAllTicketAccess())
                .manageProjectAccess(request.isManageProjectAccess())
                .manageUserAccess(request.isManageUserAccess())
                .manageCompanyAccess(request.isManageCompanyAccess())
                .manageDataAccess(request.isManageDataAccess())
                .build();
        return toResponse(userTypeRepository.save(userType));
    }

    @Transactional
    public UserTypeResponse update(UUID id, UserTypeRequest request) {
        UserType userType = userTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบประเภทผู้ใช้ id: " + id));
        if (userTypeRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อประเภทผู้ใช้ '" + request.getName() + "' มีอยู่แล้ว");
        }
        userType.setName(request.getName());
        userType.setMyTicketAccess(request.getMyTicketAccess());
        userType.setAllProjectAccess(request.isAllProjectAccess());
        userType.setNotificationAccess(request.isNotificationAccess());
        userType.setDashboardAccess(request.isDashboardAccess());
        userType.setAllTicketAccess(request.isAllTicketAccess());
        userType.setManageProjectAccess(request.isManageProjectAccess());
        userType.setManageUserAccess(request.isManageUserAccess());
        userType.setManageCompanyAccess(request.isManageCompanyAccess());
        userType.setManageDataAccess(request.isManageDataAccess());
        return toResponse(userTypeRepository.save(userType));
    }

    @Transactional
    public void delete(UUID id) {
        if (!userTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("ไม่พบประเภทผู้ใช้ id: " + id);
        }
        userTypeRepository.deleteById(id);
    }

    private UserTypeResponse toResponse(UserType userType) {
        return UserTypeResponse.builder()
                .id(userType.getId())
                .name(userType.getName())
                .myTicketAccess(userType.getMyTicketAccess())
                .allProjectAccess(userType.isAllProjectAccess())
                .notificationAccess(userType.isNotificationAccess())
                .dashboardAccess(userType.isDashboardAccess())
                .allTicketAccess(userType.isAllTicketAccess())
                .manageProjectAccess(userType.isManageProjectAccess())
                .manageUserAccess(userType.isManageUserAccess())
                .manageCompanyAccess(userType.isManageCompanyAccess())
                .manageDataAccess(userType.isManageDataAccess())
                .createdAt(userType.getCreatedAt())
                .updatedAt(userType.getUpdatedAt())
                .build();
    }
}
