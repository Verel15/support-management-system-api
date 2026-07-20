package com.ticket.support_management_system_api.features.user.service;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.enums.CommonStatus;
import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.company.entities.Company;
import com.ticket.support_management_system_api.features.company.repository.CompanyRepository;
import com.ticket.support_management_system_api.features.department.entities.Department;
import com.ticket.support_management_system_api.features.department.repository.DepartmentRepository;
import com.ticket.support_management_system_api.features.position.entities.Position;
import com.ticket.support_management_system_api.features.position.repository.PositionRepository;
import com.ticket.support_management_system_api.features.user.dto.UserFilterRequest;
import com.ticket.support_management_system_api.features.user.dto.UserRequest;
import com.ticket.support_management_system_api.features.user.dto.UserResponse;
import com.ticket.support_management_system_api.features.user.entities.CustomerDetails;
import com.ticket.support_management_system_api.features.user.entities.StaffDetails;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.CustomerDetailsRepository;
import com.ticket.support_management_system_api.features.user.repository.StaffDetailsRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import com.ticket.support_management_system_api.features.user.repository.UserSpecification;
import com.ticket.support_management_system_api.features.user_type.entities.UserType;
import com.ticket.support_management_system_api.features.user_type.repository.UserTypeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final StaffDetailsRepository staffDetailsRepository;
    private final CompanyRepository companyRepository;
    private final UserTypeRepository userTypeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(UserFilterRequest filter, Pageable pageable) {
        Page<User> page = userRepository.findAll(UserSpecification.active(filter), pageable);
        List<UUID> ids = page.getContent().stream().map(user -> user.getId()).toList();

        Map<UUID, CustomerDetails> customerMap = customerDetailsRepository.findAllByUserIdIn(ids)
                .stream().collect(Collectors.toMap(cd -> cd.getUserId(), cd -> cd));
        Map<UUID, StaffDetails> staffMap = staffDetailsRepository.findAllByUserIdIn(ids)
                .stream().collect(Collectors.toMap(sd -> sd.getUserId(), sd -> sd));

        return PaginationUtils.toPageResponse(page, user -> toResponse(user, customerMap, staffMap));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return toResponse(findActiveUser(id));
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("อีเมล '" + request.getEmail() + "' มีอยู่ในระบบแล้ว");
        }

        User user = User.builder()
                .accountType(request.getAccountType())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(generatePassword()))
                .profileImageUrl(request.getProfileImageUrl())
                .status(CommonStatus.ACTIVE)
                .build();
        user = userRepository.save(user);
        if (request.getAccountType() == AccountType.CUSTOMER) {
            saveCustomerDetails(user, request);
        } else if (request.getAccountType() == AccountType.STAFF) {
            saveStaffDetails(user, request);
        }

        return toResponse(user);
    }

    public UserResponse update(UUID id, UserRequest request) {
        User user = findActiveUser(id);

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("อีเมล '" + request.getEmail() + "' มีอยู่ในระบบแล้ว");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setProfileImageUrl(request.getProfileImageUrl());

        user = userRepository.save(user);

        if (user.getAccountType() == AccountType.CUSTOMER) {
            saveCustomerDetails(user, request);
        } else if (user.getAccountType() == AccountType.STAFF) {
            saveStaffDetails(user, request);
        }

        return toResponse(user);
    }

    public void delete(UUID id) {
        User user = findActiveUser(id);
        user.setArchivedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void saveCustomerDetails(User user, UserRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลบริษัท id: " + request.getCompanyId()));

        CustomerDetails details = customerDetailsRepository.findById(user.getId())
                .orElse(CustomerDetails.builder().user(user).build());
        details.setCompany(company);
        customerDetailsRepository.save(details);
    }

    private void saveStaffDetails(User user, UserRequest request) {
        UserType userType = userTypeRepository.findById(request.getUserTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลประเภทผู้ใช้ id: " + request.getUserTypeId()));

        Department department = request.getDepartmentId() != null
                ? departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลแผนก id: " + request.getDepartmentId()))
                : null;

        Position position = request.getPositionId() != null
                ? positionRepository.findById(request.getPositionId())
                        .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลตำแหน่ง id: " + request.getPositionId()))
                : null;

        StaffDetails details = staffDetailsRepository.findById(user.getId())
                .orElse(StaffDetails.builder().user(user).build());
        details.setUserType(userType);
        details.setDepartment(department);
        details.setPosition(position);
        staffDetailsRepository.save(details);
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        System.out.println("Generated Password: " + sb.toString());
        return sb.toString();
    }

    private User findActiveUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลผู้ใช้ id: " + id));
        if (user.getArchivedAt() != null) {
            throw new ResourceNotFoundException("ไม่พบข้อมูลผู้ใช้ id: " + id);
        }
        return user;
    }

    private UserResponse toResponse(User user) {
        CustomerDetails cd = user.getAccountType() == AccountType.CUSTOMER
                ? customerDetailsRepository.findById(user.getId()).orElse(null) : null;
        StaffDetails sd = user.getAccountType() == AccountType.STAFF
                ? staffDetailsRepository.findById(user.getId()).orElse(null) : null;
        return buildResponse(user, cd, sd);
    }

    private UserResponse toResponse(User user, Map<UUID, CustomerDetails> customerMap, Map<UUID, StaffDetails> staffMap) {
        return buildResponse(user, customerMap.get(user.getId()), staffMap.get(user.getId()));
    }

    private UserResponse buildResponse(User user, CustomerDetails cd, StaffDetails sd) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .accountType(user.getAccountType())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImageUrl(user.getProfileImageUrl())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (cd != null && cd.getCompany() != null) {
            builder.companyId(cd.getCompany().getId());
            builder.companyName(cd.getCompany().getName());
        }
        if (sd != null) {
            if (sd.getUserType() != null) {
                builder.userTypeId(sd.getUserType().getId());
                builder.userTypeName(sd.getUserType().getName());
            }
            if (sd.getDepartment() != null) {
                builder.departmentId(sd.getDepartment().getId());
                builder.departmentName(sd.getDepartment().getName());
            }
            if (sd.getPosition() != null) {
                builder.positionId(sd.getPosition().getId());
                builder.positionName(sd.getPosition().getName());
            }
        }

        return builder.build();
    }
}
