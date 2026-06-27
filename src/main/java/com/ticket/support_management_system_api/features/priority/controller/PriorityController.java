package com.ticket.support_management_system_api.features.priority.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.priority.dto.PriorityRequest;
import com.ticket.support_management_system_api.features.priority.dto.PriorityResponse;
import com.ticket.support_management_system_api.features.priority.service.PriorityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/priorities")
@RequiredArgsConstructor
public class PriorityController {

    private final PriorityService priorityService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PriorityResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(priorityService.findAll(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PriorityResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(priorityService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PriorityResponse>> create(@Valid @RequestBody PriorityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างลำดับความสำคัญสำเร็จ", priorityService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PriorityResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PriorityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตลำดับความสำคัญสำเร็จ", priorityService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal user) {
        priorityService.delete(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบลำดับความสำคัญสำเร็จ", null));
    }
}
