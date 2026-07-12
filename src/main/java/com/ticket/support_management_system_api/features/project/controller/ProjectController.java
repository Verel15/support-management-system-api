package com.ticket.support_management_system_api.features.project.controller;

import com.ticket.support_management_system_api.common.dto.DeleteConfirmationRequest;
import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.service.ReauthenticationService;
import com.ticket.support_management_system_api.features.project.dto.ProjectFilterRequest;
import com.ticket.support_management_system_api.features.project.dto.ProjectRequest;
import com.ticket.support_management_system_api.features.project.dto.ProjectResponse;
import com.ticket.support_management_system_api.features.project.dto.ProjectTicketStatsResponse;
import com.ticket.support_management_system_api.features.project.service.ProjectService;
import com.ticket.support_management_system_api.features.ticket.dto.TicketFilterRequest;
import com.ticket.support_management_system_api.features.ticket.dto.TicketListResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ReauthenticationService reauthenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> findAll(
            @ModelAttribute ProjectFilterRequest filter,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findById(id)));
    }

    @GetMapping("/{id}/ticket-stats")
    public ResponseEntity<ApiResponse<ProjectTicketStatsResponse>> getTicketStats(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getTicketStats(id)));
    }

    @GetMapping("/{id}/tickets")
    public ResponseEntity<ApiResponse<PageResponse<TicketListResponse>>> findTicketsByProjectId(
            @PathVariable UUID id,
            @ModelAttribute TicketFilterRequest filter,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findTicketsByProjectId(id, filter, pageable)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> findMy(
            @ModelAttribute ProjectFilterRequest filter,
            Pageable pageable,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findMy(filter, pageable, user)));
    }

    @GetMapping("/my/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> findMyById(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findMyById(id, user)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างโครงการสำเร็จ", projectService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตโครงการสำเร็จ", projectService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @Valid @RequestBody DeleteConfirmationRequest body,
            @AuthenticationPrincipal JwtPrincipal user,
            HttpServletRequest request) {
        reauthenticationService.verifyPassword(user.userId(), body.getPassword(), request);
        projectService.delete(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบโครงการสำเร็จ", null));
    }
}
