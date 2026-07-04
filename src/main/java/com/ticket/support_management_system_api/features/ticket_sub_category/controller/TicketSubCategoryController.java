package com.ticket.support_management_system_api.features.ticket_sub_category.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.ticket.support_management_system_api.features.ticket_sub_category.dto.TicketSubCategoryFilterRequest;
import com.ticket.support_management_system_api.features.ticket_sub_category.dto.TicketSubCategoryRequest;
import com.ticket.support_management_system_api.features.ticket_sub_category.dto.TicketSubCategoryResponse;
import com.ticket.support_management_system_api.features.ticket_sub_category.service.TicketSubCategoryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ticket-sub-categories")
@RequiredArgsConstructor
public class TicketSubCategoryController {

    private final TicketSubCategoryService ticketSubCategoryService;
    private final ReauthenticationService reauthenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TicketSubCategoryResponse>>> findAll(
            @ModelAttribute TicketSubCategoryFilterRequest filter,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(ticketSubCategoryService.findAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketSubCategoryResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(ticketSubCategoryService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketSubCategoryResponse>> create(
            @Valid @RequestBody TicketSubCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างหมวดหมู่ย่อยสำเร็จ", ticketSubCategoryService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketSubCategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TicketSubCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตหมวดหมู่ย่อยสำเร็จ",
                ticketSubCategoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @Valid @RequestBody DeleteConfirmationRequest body,
            @AuthenticationPrincipal JwtPrincipal user,
            HttpServletRequest request) {
        reauthenticationService.verifyPassword(user.userId(), body.getPassword(), request);
        ticketSubCategoryService.delete(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบหมวดหมู่ย่อยสำเร็จ", null));
    }
}
