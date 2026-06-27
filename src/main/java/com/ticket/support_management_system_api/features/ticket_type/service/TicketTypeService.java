package com.ticket.support_management_system_api.features.ticket_type.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.ticket.support_management_system_api.features.ticket_category.entities.TicketCategory;
import com.ticket.support_management_system_api.features.ticket_category.repository.TicketCategoryRepository;
import com.ticket.support_management_system_api.features.ticket_type.dto.TicketTypeRequest;
import com.ticket.support_management_system_api.features.ticket_type.dto.TicketTypeResponse;
import com.ticket.support_management_system_api.features.ticket_type.dto.TicketTypeSelectorResponse;
import com.ticket.support_management_system_api.features.ticket_type.entities.TicketType;
import com.ticket.support_management_system_api.features.ticket_type.repository.TicketTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final TicketCategoryRepository ticketCategoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<TicketTypeResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return PaginationUtils.toPageResponse(
                ticketTypeRepository.findAllByArchivedAtIsNullOrderByCreatedAt(pageable),
                this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public TicketTypeResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public TicketTypeResponse create(TicketTypeRequest request) {
        if (ticketTypeRepository.existsByNameAndArchivedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("ชื่อประเภทตั๋ว '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        List<TicketCategory> categories = getCategoriesOrThrow(request.getCategoryIds());
        TicketType ticketType = TicketType.builder()
                .name(request.getName())
                .categories(new ArrayList<>(categories))
                .build();
        return toResponse(ticketTypeRepository.saveAndFlush(ticketType));
    }

    public TicketTypeResponse update(UUID id, TicketTypeRequest request) {
        TicketType ticketType = getOrThrow(id);
        if (ticketTypeRepository.existsByNameAndArchivedAtIsNullAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อประเภทตั๋ว '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        List<TicketCategory> categories = getCategoriesOrThrow(request.getCategoryIds());
        ticketType.setName(request.getName());
        ticketType.getCategories().clear();
        ticketType.getCategories().addAll(categories);
        return toResponse(ticketTypeRepository.save(ticketType));
    }

    public void delete(UUID id, UUID userId) {
        TicketType ticketType = getOrThrow(id);
        ticketType.setArchivedAt(LocalDateTime.now());
        ticketType.setArchivedBy(userId);
        ticketTypeRepository.save(ticketType);
    }

    @Transactional(readOnly = true)
    public List<TicketTypeSelectorResponse> findAllForSelector() {
        return ticketTypeRepository.findAllByArchivedAtIsNull().stream()
                .map(type -> TicketTypeSelectorResponse.builder()
                        .id(type.getId())
                        .name(type.getName())
                        .categories(type.getCategories().stream()
                                .filter(c -> c.getArchivedAt() == null)
                                .map(cat -> TicketTypeSelectorResponse.CategoryItem.builder()
                                        .id(cat.getId())
                                        .name(cat.getName())
                                        .statusFlowId(cat.getStatusFlow().getId())
                                        .statusFlowName(cat.getStatusFlow().getName())
                                        .subCategories(cat.getSubCategories().stream()
                                                .filter(sc -> sc.getArchivedAt() == null)
                                                .map(sub -> TicketTypeSelectorResponse.SubCategoryItem.builder()
                                                        .id(sub.getId())
                                                        .name(sub.getName())
                                                        .priorityLevelId(sub.getPriorityLevel().getId())
                                                        .priorityLevelName(sub.getPriorityLevel().getName())
                                                        .positionId(sub.getPosition().getId())
                                                        .positionName(sub.getPosition().getName())
                                                        .build())
                                                .toList())
                                        .build())
                                .toList())
                        .build())
                .toList();
    }

    public TicketType getOrThrow(UUID id) {
        return ticketTypeRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบประเภทตั๋ว id: " + id));
    }

    private List<TicketCategory> getCategoriesOrThrow(List<UUID> ids) {
        List<TicketCategory> categories = ticketCategoryRepository.findAllByIdInAndArchivedAtIsNull(ids);
        if (categories.size() != ids.size()) {
            throw new ResourceNotFoundException("หมวดหมู่บางรายการไม่พบในระบบ");
        }
        return categories;
    }

    private TicketTypeResponse toResponse(TicketType ticketType) {
        return TicketTypeResponse.builder()
                .id(ticketType.getId())
                .name(ticketType.getName())
                .createdAt(ticketType.getCreatedAt())
                .updatedAt(ticketType.getUpdatedAt())
                .build();
    }
}
