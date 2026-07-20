package com.ticket.support_management_system_api.features.ticket_category.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticket.support_management_system_api.common.dto.DeleteConfirmationRequest;
import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.service.ReauthenticationService;
import com.ticket.support_management_system_api.features.ticket_category.dto.TicketCategoryFilterRequest;
import com.ticket.support_management_system_api.features.ticket_category.dto.TicketCategoryRequest;
import com.ticket.support_management_system_api.features.ticket_category.dto.TicketCategoryResponse;
import com.ticket.support_management_system_api.features.ticket_category.service.TicketCategoryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ticket-categories")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;
    private final ReauthenticationService reauthenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TicketCategoryResponse>>> findAll(
            @ModelAttribute TicketCategoryFilterRequest filter,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(ticketCategoryService.findAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketCategoryResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(ticketCategoryService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_manageDataAccess')")
    public ResponseEntity<ApiResponse<TicketCategoryResponse>> create(@Valid @RequestBody TicketCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างหมวดหมู่สำเร็จ", ticketCategoryService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_manageDataAccess')")
    public ResponseEntity<ApiResponse<TicketCategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TicketCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตหมวดหมู่สำเร็จ", ticketCategoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_manageDataAccess')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @Valid @RequestBody DeleteConfirmationRequest body,
            @AuthenticationPrincipal JwtPrincipal user,
            HttpServletRequest request) {
        reauthenticationService.verifyPassword(user.userId(), body.getPassword(), request);
        ticketCategoryService.delete(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบหมวดหมู่สำเร็จ", null));
    }
}
