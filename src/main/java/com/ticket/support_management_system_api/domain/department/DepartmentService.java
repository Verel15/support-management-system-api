package com.ticket.support_management_system_api.domain.department;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.domain.department.dto.DepartmentRequest;
import com.ticket.support_management_system_api.domain.department.dto.DepartmentResponse;
import com.ticket.support_management_system_api.domain.department.entities.Department;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAllActiveOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลแผนก id: " + id));
        return toResponse(department);
    }

    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("ชื่อแผนก '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        Department department = Department.builder()
                .name(request.getName())
                .build();
        return toResponse(departmentRepository.save(department));
    }

    public DepartmentResponse update(UUID id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลแผนก id: " + id));
        if (departmentRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อแผนก '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        department.setName(request.getName());
        return toResponse(departmentRepository.save(department));
    }

    public void delete(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("ไม่พบข้อมูลแผนก id: " + id);
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentResponse toResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }
}
