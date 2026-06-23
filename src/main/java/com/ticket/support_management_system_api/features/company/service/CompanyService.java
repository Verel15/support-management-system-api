package com.ticket.support_management_system_api.features.company.service;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.company.dto.CompanyRequest;
import com.ticket.support_management_system_api.features.company.dto.CompanyResponse;
import com.ticket.support_management_system_api.features.company.entities.Company;
import com.ticket.support_management_system_api.features.company.repository.CompanyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companyRepository.findAllByArchivedAtIsNullOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลบริษัท id: " + id));
        return toResponse(company);
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

    private CompanyResponse toResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoImageUrl(company.getLogoImageUrl())
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
