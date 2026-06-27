package com.ticket.support_management_system_api.features.ticket_type.controller;

import java.util.List;
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
import com.ticket.support_management_system_api.features.ticket_type.dto.TicketTypeRequest;
import com.ticket.support_management_system_api.features.ticket_type.dto.TicketTypeResponse;
import com.ticket.support_management_system_api.features.ticket_type.dto.TicketTypeSelectorResponse;
import com.ticket.support_management_system_api.features.ticket_type.service.TicketTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TicketTypeResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(ticketTypeService.findAll(page, size)));
    }

    @GetMapping("/selector")
    public ResponseEntity<ApiResponse<List<TicketTypeSelectorResponse>>> findAllForSelector() {
        return ResponseEntity.ok(ApiResponse.success(ticketTypeService.findAllForSelector()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketTypeResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(ticketTypeService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketTypeResponse>> create(@Valid @RequestBody TicketTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างประเภทตั๋วสำเร็จ", ticketTypeService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketTypeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TicketTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตประเภทตั๋วสำเร็จ", ticketTypeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal user) {
        ticketTypeService.delete(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบประเภทตั๋วสำเร็จ", null));
    }
}
