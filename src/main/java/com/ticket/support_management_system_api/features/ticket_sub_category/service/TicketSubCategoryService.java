package com.ticket.support_management_system_api.features.ticket_sub_category.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.position.entities.Position;
import com.ticket.support_management_system_api.features.position.repository.PositionRepository;
import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;
import com.ticket.support_management_system_api.features.priority.repository.PriorityRepository;
import com.ticket.support_management_system_api.features.ticket_category.repository.TicketCategoryRepository;
import com.ticket.support_management_system_api.features.ticket_sub_category.dto.TicketSubCategoryRequest;
import com.ticket.support_management_system_api.features.ticket_sub_category.dto.TicketSubCategoryResponse;
import com.ticket.support_management_system_api.features.ticket_sub_category.entities.TicketSubCategory;
import com.ticket.support_management_system_api.features.ticket_sub_category.repository.TicketSubCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketSubCategoryService {

    private final TicketSubCategoryRepository ticketSubCategoryRepository;
    private final PriorityRepository priorityRepository;
    private final PositionRepository positionRepository;
    private final TicketCategoryRepository ticketCategoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<TicketSubCategoryResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PaginationUtils.toPageResponse(
                ticketSubCategoryRepository.findAllByArchivedAtIsNull(pageable),
                this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public TicketSubCategoryResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public TicketSubCategoryResponse create(TicketSubCategoryRequest request) {
        PriorityLevels priority = getPriorityOrThrow(request.getPriorityLevelId());
        Position position = getPositionOrThrow(request.getPositionId());

        if (ticketSubCategoryRepository.existsByNameAndArchivedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("ชื่อหมวดหมู่ย่อย '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }

        TicketSubCategory subCategory = TicketSubCategory.builder()
                .name(request.getName())
                .priorityLevel(priority)
                .position(position)
                .build();
        return toResponse(ticketSubCategoryRepository.saveAndFlush(subCategory));
    }

    public TicketSubCategoryResponse update(UUID id, TicketSubCategoryRequest request) {
        TicketSubCategory subCategory = getOrThrow(id);
        PriorityLevels priority = getPriorityOrThrow(request.getPriorityLevelId());
        Position position = getPositionOrThrow(request.getPositionId());

        if (ticketSubCategoryRepository.existsByNameAndArchivedAtIsNullAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อหมวดหมู่ย่อย '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }

        subCategory.setName(request.getName());
        subCategory.setPriorityLevel(priority);
        subCategory.setPosition(position);
        return toResponse(ticketSubCategoryRepository.save(subCategory));
    }

    public void delete(UUID id, UUID userId) {
        TicketSubCategory subCategory = getOrThrow(id);
        if (ticketCategoryRepository.existsBySubCategoriesIdAndArchivedAtIsNull(id)) {
            throw new DuplicateResourceException("ไม่สามารถลบหมวดหมู่ย่อยที่ยังถูกใช้งานโดยหมวดหมู่อยู่");
        }
        subCategory.setArchivedAt(LocalDateTime.now());
        subCategory.setArchivedBy(userId);
        ticketSubCategoryRepository.save(subCategory);
    }

    public TicketSubCategory getOrThrow(UUID id) {
        return ticketSubCategoryRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบหมวดหมู่ย่อย id: " + id));
    }

    private PriorityLevels getPriorityOrThrow(UUID id) {
        return priorityRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบลำดับความสำคัญ id: " + id));
    }

    private Position getPositionOrThrow(UUID id) {
        return positionRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบตำแหน่ง id: " + id));
    }

    public TicketSubCategoryResponse toResponse(TicketSubCategory subCategory) {
        return TicketSubCategoryResponse.builder()
                .id(subCategory.getId())
                .name(subCategory.getName())
                .priorityLevelId(subCategory.getPriorityLevel().getId())
                .priorityLevelName(subCategory.getPriorityLevel().getName())
                .positionId(subCategory.getPosition().getId())
                .positionName(subCategory.getPosition().getName())
                .createdAt(subCategory.getCreatedAt())
                .updatedAt(subCategory.getUpdatedAt())
                .build();
    }
}
