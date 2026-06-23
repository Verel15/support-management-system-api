package com.ticket.support_management_system_api.features.department.repository;

import com.ticket.support_management_system_api.features.department.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, UUID id);
    List<Department> findAllByArchivedAtIsNullOrderByNameAsc();
}
