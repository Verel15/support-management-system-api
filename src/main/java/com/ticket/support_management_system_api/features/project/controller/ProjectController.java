package com.ticket.support_management_system_api.features.project.controller;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.project.dto.ProjectRequest;
import com.ticket.support_management_system_api.features.project.dto.ProjectResponse;
import com.ticket.support_management_system_api.features.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findAll(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findById(id)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> findMy(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal JwtPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findMy(page, size, user)));
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
            @AuthenticationPrincipal JwtPrincipal user) {
        projectService.delete(id, user.userId());
        return ResponseEntity.ok(ApiResponse.success("ลบโครงการสำเร็จ", null));
    }
}
