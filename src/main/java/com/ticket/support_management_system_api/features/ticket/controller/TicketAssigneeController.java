package com.ticket.support_management_system_api.features.ticket.controller;

import com.ticket.support_management_system_api.common.dto.DeleteConfirmationRequest;
import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.service.ReauthenticationService;
import com.ticket.support_management_system_api.features.ticket.dto.AddAssigneeRequest;
import com.ticket.support_management_system_api.features.ticket.dto.TicketAssigneeResponse;
import com.ticket.support_management_system_api.features.ticket.service.TicketAssigneeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/assignees")
@RequiredArgsConstructor
public class TicketAssigneeController {

    private final TicketAssigneeService assigneeService;
    private final ReauthenticationService reauthenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketAssigneeResponse>>> findAll(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ApiResponse.success(assigneeService.findAllByTicket(ticketId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketAssigneeResponse>> addAssignee(
            @PathVariable UUID ticketId,
            @Valid @RequestBody AddAssigneeRequest request,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("เพิ่มผู้รับผิดชอบสำเร็จ", assigneeService.addAssignee(ticketId, request)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeAssignee(
            @PathVariable UUID ticketId,
            @PathVariable UUID userId,
            @Valid @RequestBody DeleteConfirmationRequest body,
            @AuthenticationPrincipal JwtPrincipal user,
            HttpServletRequest request) {
        reauthenticationService.verifyPassword(user.userId(), body.getPassword(), request);
        assigneeService.removeAssignee(ticketId, userId, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบผู้รับผิดชอบสำเร็จ", null));
    }
}
