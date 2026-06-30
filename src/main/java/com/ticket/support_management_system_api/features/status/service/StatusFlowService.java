package com.ticket.support_management_system_api.features.status.service;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.status.dto.StatusFlowRequest;
import com.ticket.support_management_system_api.features.status.dto.StatusFlowResponse;
import com.ticket.support_management_system_api.features.status.entities.StatusFlows;
import com.ticket.support_management_system_api.features.status.entities.Statuses;
import com.ticket.support_management_system_api.features.status.repository.StatusFlowRepository;
import com.ticket.support_management_system_api.features.status.repository.StatusRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket.support_management_system_api.features.status.enums.StatusGroup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StatusFlowService {

    private final StatusFlowRepository statusFlowRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<StatusFlowResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return PaginationUtils.toPageResponse(
                statusFlowRepository.findAllByArchivedAtIsNull(pageable),
                this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public StatusFlowResponse findById(UUID id) {
        StatusFlows flow = getFlowOrThrow(id);
        return toResponse(flow);
    }

    public StatusFlowResponse create(StatusFlowRequest request, JwtPrincipal user) {
        if (statusFlowRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("ชื่อสถานะ '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        StatusFlows flow = StatusFlows.builder()
                .name(request.getName())
                .build();
        flow = statusFlowRepository.save(flow);

        List<Statuses> statuses = buildAllStatuses(request.getProcessStatuses(), flow);
        statusRepository.saveAll(statuses);

        return toResponse(flow, statuses);
    }

    public StatusFlowResponse update(UUID id, StatusFlowRequest request) {
        StatusFlows flow = getFlowOrThrow(id);
        if (statusFlowRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อสถานะ '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        flow.setName(request.getName());
        statusFlowRepository.save(flow);

        statusRepository.deleteByFlowId(id);
        List<Statuses> statuses = buildAllStatuses(request.getProcessStatuses(), flow);
        statusRepository.saveAll(statuses);

        return toResponse(flow, statuses);
    }

    public void delete(UUID id, UUID userId) {
        StatusFlows flow = getFlowOrThrow(id);
        flow.setArchivedAt(LocalDateTime.now());
        flow.setArchivedBy(userId);
        statusFlowRepository.save(flow);
    }

    private StatusFlows getFlowOrThrow(UUID id) {
        return statusFlowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลสถานะ id: " + id));
    }

    private List<Statuses> buildAllStatuses(List<String> processNames, StatusFlows flow) {
        List<Statuses> statuses = new ArrayList<>();

        statuses.add(buildStatus(flow, StatusGroup.START, "Open", 1, true));

        int processSeq = 1;
        statuses.add(buildStatus(flow, StatusGroup.PROCESS, "In Progress", processSeq++, true));
        if (processNames != null) {
            for (String name : processNames) {
                statuses.add(buildStatus(flow, StatusGroup.PROCESS, name, processSeq++, false));
            }
        }

        statuses.add(buildStatus(flow, StatusGroup.SUCCESS, "Done", 1, true));
        statuses.add(buildStatus(flow, StatusGroup.SUCCESS, "Close", 2, true));
        statuses.add(buildStatus(flow, StatusGroup.FAILED, "Reject", 1, true));
        statuses.add(buildStatus(flow, StatusGroup.FAILED, "Return", 2, true));

        return statuses;
    }

    private Statuses buildStatus(StatusFlows flow, StatusGroup group, String name, int sequence, boolean isSystem) {
        return Statuses.builder()
                .flow(flow)
                .group(group)
                .name(name)
                .sequence(sequence)
                .isSystem(isSystem)
                .build();
    }

    private StatusFlowResponse toResponse(StatusFlows flow) {
        List<Statuses> statuses = statusRepository.findByFlowId(flow.getId());
        return toResponse(flow, statuses);
    }

    private StatusFlowResponse toResponse(StatusFlows flow, List<Statuses> statuses) {
        return StatusFlowResponse.builder()
                .id(flow.getId())
                .name(flow.getName())
                .statuses(statuses.stream().map(this::toStatusItemResponse).toList())
                .ticketCount(0)
                .createdBy(flow.getCreatedBy())
                .createdByName(resolveFullName(flow.getCreatedBy()))
                .createdAt(flow.getCreatedAt())
                .updatedByName(resolveFullName(flow.getUpdatedBy()))
                .updatedAt(flow.getUpdatedAt())
                .build();
    }

    private String resolveFullName(UUID userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse(null);
    }

    private StatusFlowResponse.StatusItemResponse toStatusItemResponse(Statuses status) {
        return StatusFlowResponse.StatusItemResponse.builder()
                .id(status.getId())
                .group(status.getGroup())
                .name(status.getName())
                .sequence(status.getSequence())
                .isSystem(status.getIsSystem())
                .build();
    }
}
