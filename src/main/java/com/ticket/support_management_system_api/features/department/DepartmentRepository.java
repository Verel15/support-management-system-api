package com.ticket.support_management_system_api.features.department;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticket.support_management_system_api.features.department.entities.Department;

import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID>, DepartmentRepositoryCustom {
}
