package com.ticket.support_management_system_api.features.status.controller;

import com.ticket.support_management_system_api.common.dto.DeleteConfirmationRequest;
import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.service.ReauthenticationService;
import com.ticket.support_management_system_api.features.status.dto.StatusFlowRequest;
import com.ticket.support_management_system_api.features.status.dto.StatusFlowResponse;
import com.ticket.support_management_system_api.features.status.service.StatusFlowService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/status-flows")
@RequiredArgsConstructor
public class StatusFlowController {

    private final StatusFlowService statusFlowService;
    private final ReauthenticationService reauthenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StatusFlowResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(statusFlowService.findAll(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StatusFlowResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(statusFlowService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StatusFlowResponse>> create(
            @Valid @RequestBody StatusFlowRequest request,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างสถานะสำเร็จ", statusFlowService.create(request, user)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StatusFlowResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody StatusFlowRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตสถานะสำเร็จ", statusFlowService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @Valid @RequestBody DeleteConfirmationRequest body,
            @AuthenticationPrincipal JwtPrincipal user,
            HttpServletRequest request) {
        reauthenticationService.verifyPassword(user.userId(), body.getPassword(), request);
        statusFlowService.delete(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบสถานะสำเร็จ", null));
    }
}
