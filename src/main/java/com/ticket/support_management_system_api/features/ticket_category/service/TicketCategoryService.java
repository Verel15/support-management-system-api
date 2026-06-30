package com.ticket.support_management_system_api.features.ticket_category.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.status.entities.StatusFlows;
import com.ticket.support_management_system_api.features.status.repository.StatusFlowRepository;
import com.ticket.support_management_system_api.features.ticket_category.dto.TicketCategoryRequest;
import com.ticket.support_management_system_api.features.ticket_category.dto.TicketCategoryResponse;
import com.ticket.support_management_system_api.features.ticket_category.entities.TicketCategory;
import com.ticket.support_management_system_api.features.ticket_category.repository.TicketCategoryRepository;
import com.ticket.support_management_system_api.features.ticket_sub_category.entities.TicketSubCategory;
import com.ticket.support_management_system_api.features.ticket_sub_category.repository.TicketSubCategoryRepository;
import com.ticket.support_management_system_api.features.ticket_type.repository.TicketTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketSubCategoryRepository ticketSubCategoryRepository;
    private final StatusFlowRepository statusFlowRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional(readOnly = true)
    public PageResponse<TicketCategoryResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PaginationUtils.toPageResponse(
                ticketCategoryRepository.findAllByArchivedAtIsNull(pageable),
                this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public TicketCategoryResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public TicketCategoryResponse create(TicketCategoryRequest request) {
        StatusFlows statusFlow = getStatusFlowOrThrow(request.getStatusFlowId());
        List<TicketSubCategory> subCategories = getSubCategoriesOrThrow(request.getSubCategoryIds());

        if (ticketCategoryRepository.existsByNameAndArchivedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("ชื่อหมวดหมู่ '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }

        TicketCategory category = TicketCategory.builder()
                .name(request.getName())
                .statusFlow(statusFlow)
                .build();
        subCategories.forEach(sc -> sc.setCategory(category));
        category.getSubCategories().addAll(subCategories);
        return toResponse(ticketCategoryRepository.saveAndFlush(category));
    }

    public TicketCategoryResponse update(UUID id, TicketCategoryRequest request) {
        TicketCategory category = getOrThrow(id);
        StatusFlows statusFlow = getStatusFlowOrThrow(request.getStatusFlowId());
        List<TicketSubCategory> subCategories = getSubCategoriesOrThrow(request.getSubCategoryIds());

        if (ticketCategoryRepository.existsByNameAndArchivedAtIsNullAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อหมวดหมู่ '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }

        category.setName(request.getName());
        category.setStatusFlow(statusFlow);
        category.getSubCategories().clear();
        subCategories.forEach(sc -> sc.setCategory(category));
        category.getSubCategories().addAll(subCategories);
        return toResponse(ticketCategoryRepository.save(category));
    }

    public void delete(UUID id, UUID userId) {
        TicketCategory category = getOrThrow(id);
        if (ticketTypeRepository.existsByCategoriesIdAndArchivedAtIsNull(id)) {
            throw new DuplicateResourceException("ไม่สามารถลบหมวดหมู่ที่ยังถูกใช้งานโดยประเภทตั๋วอยู่");
        }
        category.setArchivedAt(LocalDateTime.now());
        category.setArchivedBy(userId);
        ticketCategoryRepository.save(category);
    }

    public TicketCategory getOrThrow(UUID id) {
        return ticketCategoryRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบหมวดหมู่ id: " + id));
    }

    private StatusFlows getStatusFlowOrThrow(UUID id) {
        return statusFlowRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบ Status Flow id: " + id));
    }

    private List<TicketSubCategory> getSubCategoriesOrThrow(List<UUID> ids) {
        List<TicketSubCategory> subCategories = ticketSubCategoryRepository.findAllByIdInAndArchivedAtIsNull(ids);
        if (subCategories.size() != ids.size()) {
            throw new ResourceNotFoundException("หมวดหมู่ย่อยบางรายการไม่พบในระบบ");
        }
        return subCategories;
    }

    public TicketCategoryResponse toResponse(TicketCategory category) {
        return TicketCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .statusFlowId(category.getStatusFlow().getId())
                .statusFlowName(category.getStatusFlow().getName())
                .subCategories(category.getSubCategories().stream()
                        .filter(sc -> sc.getArchivedAt() == null)
                        .map(sc -> TicketCategoryResponse.SubCategoryItem.builder()
                                .id(sc.getId())
                                .name(sc.getName())
                                .build())
                        .toList())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
