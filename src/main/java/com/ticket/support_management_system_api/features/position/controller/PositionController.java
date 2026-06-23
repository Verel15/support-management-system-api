package com.ticket.support_management_system_api.features.position.controller;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.features.position.dto.PositionRequest;
import com.ticket.support_management_system_api.features.position.dto.PositionResponse;
import com.ticket.support_management_system_api.features.position.service.PositionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PositionResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(positionService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(positionService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PositionResponse>> create(@Valid @RequestBody PositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างข้อมูลตำแหน่งสำเร็จ", positionService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PositionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PositionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตข้อมูลตำแหน่งสำเร็จ", positionService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        positionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("ลบข้อมูลตำแหน่งสำเร็จ", null));
    }
}
