package com.ticket.support_management_system_api.features.priority.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.priority.dto.PriorityFilterRequest;
import com.ticket.support_management_system_api.features.priority.dto.PriorityRequest;
import com.ticket.support_management_system_api.features.priority.dto.PriorityResponse;
import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;
import com.ticket.support_management_system_api.features.priority.repository.PriorityRepository;
import com.ticket.support_management_system_api.features.priority.repository.PrioritySpecification;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PriorityService {

    private final PriorityRepository priorityRepository;

    @Transactional(readOnly = true)
    public PageResponse<PriorityResponse> findAll(PriorityFilterRequest filter, Pageable pageable) {
        Page<PriorityLevels> page = priorityRepository.findAll(PrioritySpecification.active(filter), pageable);
        return PaginationUtils.toPageResponse(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public PriorityResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public PriorityResponse create(PriorityRequest request) {
        if (priorityRepository.existsByNameAndArchivedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("ชื่อลำดับความสำคัญ '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        PriorityLevels priority = PriorityLevels.builder()
                .name(request.getName())
                .description(request.getDescription())
                .iconShape(request.getIconShape())
                .iconColor(request.getIconColor())
                .intervalValue(request.getIntervalValue())
                .intervalUnit(request.getIntervalUnit())
                .build();
        return toResponse(priorityRepository.saveAndFlush(priority));
    }

    public PriorityResponse update(UUID id, PriorityRequest request) {
        PriorityLevels priority = getOrThrow(id);
        if (priorityRepository.existsByNameAndArchivedAtIsNullAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อลำดับความสำคัญ '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        priority.setName(request.getName());
        priority.setDescription(request.getDescription());
        priority.setIconShape(request.getIconShape());
        priority.setIconColor(request.getIconColor());
        priority.setIntervalValue(request.getIntervalValue());
        priority.setIntervalUnit(request.getIntervalUnit());
        return toResponse(priorityRepository.save(priority));
    }

    public void delete(UUID id, UUID userId) {
        PriorityLevels priority = getOrThrow(id);
        priority.setArchivedAt(LocalDateTime.now());
        priority.setArchivedBy(userId);
        priorityRepository.save(priority);
    }

    private PriorityLevels getOrThrow(UUID id) {
        return priorityRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบลำดับความสำคัญ id: " + id));
    }

    private PriorityResponse toResponse(PriorityLevels priority) {
        return PriorityResponse.builder()
                .id(priority.getId())
                .name(priority.getName())
                .description(priority.getDescription())
                .iconShape(priority.getIconShape())
                .iconColor(priority.getIconColor())
                .intervalValue(priority.getIntervalValue())
                .intervalUnit(priority.getIntervalUnit())
                .createdAt(priority.getCreatedAt())
                .updatedAt(priority.getUpdatedAt())
                .build();
    }
}
