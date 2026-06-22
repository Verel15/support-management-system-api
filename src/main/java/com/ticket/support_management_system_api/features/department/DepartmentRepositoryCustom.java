package com.ticket.support_management_system_api.features.department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ticket.support_management_system_api.features.department.entities.Department;

public interface DepartmentRepositoryCustom {

    Optional<Department> findByName(String name);

    List<Department> findAllOrderByNameAsc();

    List<Department> findAllActiveOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);
}
