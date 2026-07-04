package com.ticket.support_management_system_api.features.user_type.controller;

import com.ticket.support_management_system_api.common.dto.DeleteConfirmationRequest;
import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.service.ReauthenticationService;
import com.ticket.support_management_system_api.features.user_type.dto.UserTypeFilterRequest;
import com.ticket.support_management_system_api.features.user_type.dto.UserTypeRequest;
import com.ticket.support_management_system_api.features.user_type.dto.UserTypeResponse;
import com.ticket.support_management_system_api.features.user_type.service.UserTypeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user-types")
@RequiredArgsConstructor
public class UserTypeController {

    private final UserTypeService userTypeService;
    private final ReauthenticationService reauthenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserTypeResponse>>> findAll(
            @ModelAttribute UserTypeFilterRequest filter,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userTypeService.findAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserTypeResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userTypeService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserTypeResponse>> create(@Valid @RequestBody UserTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างประเภทผู้ใช้สำเร็จ", userTypeService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserTypeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตประเภทผู้ใช้สำเร็จ", userTypeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @Valid @RequestBody DeleteConfirmationRequest body,
            @AuthenticationPrincipal JwtPrincipal user,
            HttpServletRequest request) {
        reauthenticationService.verifyPassword(user.userId(), body.getPassword(), request);
        userTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("ลบประเภทผู้ใช้สำเร็จ", null));
    }
}
