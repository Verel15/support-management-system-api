package com.ticket.support_management_system_api.features.company.repository;

import com.ticket.support_management_system_api.features.company.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    List<Company> findAllByArchivedAtIsNullOrderByNameAsc();
}
