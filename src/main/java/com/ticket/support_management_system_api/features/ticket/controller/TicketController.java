package com.ticket.support_management_system_api.features.ticket.controller;

import com.ticket.support_management_system_api.common.dto.DeleteConfirmationRequest;
import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.service.ReauthenticationService;
import com.ticket.support_management_system_api.features.ticket.dto.*;
import com.ticket.support_management_system_api.features.ticket.service.RebalanceSuggestionService;
import com.ticket.support_management_system_api.features.ticket.service.TicketService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final RebalanceSuggestionService rebalanceSuggestionService;
    private final ReauthenticationService reauthenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TicketListResponse>>> findAll(
            @ModelAttribute TicketFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(ticketService.findAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.findById(id)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<TicketListResponse>>> findMy(
            @ModelAttribute TicketFilterRequest filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal JwtPrincipal user) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(ticketService.findMy(filter, pageable, user)));
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> findMyById(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.findMyById(id, user)));
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
            @Valid @RequestBody DeleteConfirmationRequest body,
            @AuthenticationPrincipal JwtPrincipal user,
            HttpServletRequest request) {
        reauthenticationService.verifyPassword(user.userId(), body.getPassword(), request);
        ticketService.delete(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบ Ticket สำเร็จ", null));
    }

    @GetMapping("/{id}/suggested-assignee")
    public ResponseEntity<ApiResponse<SuggestedAssigneeResponse>> suggestedAssignee(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getSuggestedAssignee(id)));
    }

    @GetMapping("/{id}/rebalance-suggestion")
    public ResponseEntity<ApiResponse<RebalanceSuggestionResponse>> rebalanceSuggestion(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(rebalanceSuggestionService.getSuggestion(id)));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeTicketStatusRequest request,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("เปลี่ยนสถานะ Ticket สำเร็จ", ticketService.changeStatus(id, request, user.userId())));
    }

    @PatchMapping("/{id}/due-date")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> updateDueDate(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketDueDateRequest request,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตครบกำหนด Ticket สำเร็จ", ticketService.updateDueDate(id, request, user.userId())));
    }

    @PostMapping("/{id}/satisfaction")
    public ResponseEntity<ApiResponse<TicketSatisfactionResponse>> addSatisfactionRating(
            @PathVariable UUID id,
            @Valid @RequestBody AddSatisfactionRatingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("บันทึกคะแนนความพึงพอใจสำเร็จ", ticketService.addSatisfactionRating(id, request)));
    }
}
