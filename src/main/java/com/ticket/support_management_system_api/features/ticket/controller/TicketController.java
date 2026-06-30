package com.ticket.support_management_system_api.features.ticket.controller;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.ticket.dto.*;
import com.ticket.support_management_system_api.features.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TicketListResponse>>> findAll(
            @ModelAttribute TicketFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(ticketService.findAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketDetailResponse>> create(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้าง Ticket สำเร็จ", ticketService.create(request, user.userId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดต Ticket สำเร็จ", ticketService.update(id, request, user.userId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal user) {
        ticketService.delete(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบ Ticket สำเร็จ", null));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeTicketStatusRequest request,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("เปลี่ยนสถานะ Ticket สำเร็จ", ticketService.changeStatus(id, request, user.userId())));
    }
}
