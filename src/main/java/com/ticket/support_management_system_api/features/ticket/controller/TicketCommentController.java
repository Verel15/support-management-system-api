package com.ticket.support_management_system_api.features.ticket.controller;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.ticket.dto.AddCommentRequest;
import com.ticket.support_management_system_api.features.ticket.dto.TicketCommentResponse;
import com.ticket.support_management_system_api.features.ticket.dto.TicketTimelineItem;
import com.ticket.support_management_system_api.features.ticket.service.TicketCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class TicketCommentController {

    private final TicketCommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketCommentResponse>>> findAll(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.findAllByTicket(ticketId)));
    }

    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<List<TicketTimelineItem>>> getTimeline(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getTimeline(ticketId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketCommentResponse>> addComment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("เพิ่มความคิดเห็นสำเร็จ", commentService.addComment(ticketId, request, user.userId())));
    }
}
