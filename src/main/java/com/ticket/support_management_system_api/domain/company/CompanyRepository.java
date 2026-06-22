package com.ticket.support_management_system_api.domain.company;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket.support_management_system_api.domain.company.entities.Company;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID>, CompanyRepositoryCustom {
}
