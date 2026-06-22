package com.ticket.support_management_system_api.domain.company;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ticket.support_management_system_api.domain.company.entities.Company;

public interface CompanyRepositoryCustom {

    Optional<Company> findByName(String name);

    List<Company> findAllOrderByNameAsc();

    List<Company> findAllActiveOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);
}
