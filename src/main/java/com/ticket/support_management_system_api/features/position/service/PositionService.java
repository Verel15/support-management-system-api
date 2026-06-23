package com.ticket.support_management_system_api.features.position.service;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.position.dto.PositionRequest;
import com.ticket.support_management_system_api.features.position.dto.PositionResponse;
import com.ticket.support_management_system_api.features.position.entities.Position;
import com.ticket.support_management_system_api.features.position.repository.PositionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PositionService {

    private final PositionRepository positionRepository;

    @Transactional(readOnly = true)
    public List<PositionResponse> findAll() {
        return positionRepository.findAllByArchivedAtIsNullOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PositionResponse findById(UUID id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลตำแหน่ง id: " + id));
        return toResponse(position);
    }

    public PositionResponse create(PositionRequest request) {
        if (positionRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("ชื่อตำแหน่ง '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        Position position = Position.builder()
                .name(request.getName())
                .build();
        return toResponse(positionRepository.save(position));
    }

    public PositionResponse update(UUID id, PositionRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลตำแหน่ง id: " + id));
        if (positionRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อตำแหน่ง '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        position.setName(request.getName());
        return toResponse(positionRepository.save(position));
    }

    public void delete(UUID id) {
        if (!positionRepository.existsById(id)) {
            throw new ResourceNotFoundException("ไม่พบข้อมูลตำแหน่ง id: " + id);
        }
        positionRepository.deleteById(id);
    }

    private PositionResponse toResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .name(position.getName())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }
}
