package com.ticket.support_management_system_api.domain.user;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.enums.CommonStatus;
import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.domain.company.Company;
import com.ticket.support_management_system_api.domain.company.CompanyRepository;
import com.ticket.support_management_system_api.domain.department.Department;
import com.ticket.support_management_system_api.domain.department.DepartmentRepository;
import com.ticket.support_management_system_api.domain.position.Position;
import com.ticket.support_management_system_api.domain.position.PositionRepository;
import com.ticket.support_management_system_api.domain.user.dto.UserFilterRequest;
import com.ticket.support_management_system_api.domain.user.dto.UserRequest;
import com.ticket.support_management_system_api.domain.user.dto.UserResponse;
import com.ticket.support_management_system_api.domain.user_type.UserType;
import com.ticket.support_management_system_api.domain.user_type.UserTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final ExternalDetailsRepository externalDetailsRepository;
    private final CompanyRepository companyRepository;
    private final UserTypeRepository userTypeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(UserFilterRequest filter, Pageable pageable) {
        return PaginationUtils.toPageResponse(userRepository.findAllActive(filter, pageable), this::toResponse);
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
        } else {
            saveExternalDetails(user, request);
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
        } else {
            saveExternalDetails(user, request);
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

    private void saveExternalDetails(User user, UserRequest request) {
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

        ExternalDetails details = externalDetailsRepository.findById(user.getId())
                .orElse(ExternalDetails.builder().user(user).build());
        details.setUserType(userType);
        details.setDepartment(department);
        details.setPosition(position);
        externalDetailsRepository.save(details);
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
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

        if (user.getAccountType() == AccountType.CUSTOMER) {
            customerDetailsRepository.findById(user.getId()).ifPresent(cd -> {
                Company c = cd.getCompany();
                if (c != null) {
                    builder.companyId(c.getId());
                    builder.companyName(c.getName());
                }
            });
        } else if (user.getAccountType() == AccountType.EXTERNAL) {
            externalDetailsRepository.findById(user.getId()).ifPresent(ed -> {
                UserType ut = ed.getUserType();
                if (ut != null) {
                    builder.userTypeId(ut.getId());
                    builder.userTypeName(ut.getName());
                }
                Department dept = ed.getDepartment();
                if (dept != null) {
                    builder.departmentId(dept.getId());
                    builder.departmentName(dept.getName());
                }
                Position pos = ed.getPosition();
                if (pos != null) {
                    builder.positionId(pos.getId());
                    builder.positionName(pos.getName());
                }
            });
        }

        return builder.build();
    }
}
