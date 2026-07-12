package com.ticket.support_management_system_api.features.company.service;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.company.dto.CompanyRequest;
import com.ticket.support_management_system_api.features.company.dto.CompanyResponse;
import com.ticket.support_management_system_api.features.company.entities.Company;
import com.ticket.support_management_system_api.features.company.repository.CompanyRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.user.dto.UserResponse;
import com.ticket.support_management_system_api.features.user.entities.CustomerDetails;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.CustomerDetailsRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import com.ticket.support_management_system_api.features.user.repository.UserSpecification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll(String keyword) {
        List<Company> companies = (keyword == null || keyword.isBlank())
                ? companyRepository.findAllByArchivedAtIsNullOrderByCreatedAtDesc()
                : companyRepository.findAllByArchivedAtIsNullAndNameContainingIgnoreCaseOrderByCreatedAtDesc(keyword.trim());
        return companies.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลบริษัท id: " + id));
        return toResponse(company);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findUsersByCompanyId(UUID companyId, String keyword, Pageable pageable) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("ไม่พบข้อมูลบริษัท id: " + companyId);
        }

        Page<User> page = userRepository.findAll(UserSpecification.byCompanyAndKeyword(companyId, keyword), pageable);
        List<UUID> ids = page.getContent().stream().map(User::getId).toList();

        Map<UUID, CustomerDetails> customerMap = customerDetailsRepository.findAllByUserIdIn(ids)
                .stream().collect(Collectors.toMap(CustomerDetails::getUserId, cd -> cd));

        return PaginationUtils.toPageResponse(page, user -> toUserResponse(user, customerMap.get(user.getId())));
    }

    public CompanyResponse create(CompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("ชื่อบริษัท '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        Company company = Company.builder()
                .name(request.getName())
                .logoImageUrl(request.getLogoImageUrl())
                .status(request.getStatus())
                .build();
        return toResponse(companyRepository.save(company));
    }

    public CompanyResponse update(UUID id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลบริษัท id: " + id));
        if (companyRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อบริษัท '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        company.setName(request.getName());
        company.setLogoImageUrl(request.getLogoImageUrl());
        company.setStatus(request.getStatus());
        return toResponse(companyRepository.save(company));
    }

    public void delete(UUID id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("ไม่พบข้อมูลบริษัท id: " + id);
        }
        companyRepository.deleteById(id);
    }

    private UserResponse toUserResponse(User user, CustomerDetails cd) {
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

        return builder.build();
    }

    private CompanyResponse toResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoImageUrl(company.getLogoImageUrl())
                .status(company.getStatus())
                .customerCount(customerDetailsRepository.countByCompanyId(company.getId()))
                .projectCount(projectRepository.countByCompanyIdAndArchivedAtIsNull(company.getId()))
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
