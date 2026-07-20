package com.ticket.support_management_system_api.features.company.controller;

import com.ticket.support_management_system_api.common.dto.DeleteConfirmationRequest;
import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.auth.service.ReauthenticationService;
import com.ticket.support_management_system_api.features.company.dto.CompanyRequest;
import com.ticket.support_management_system_api.features.company.dto.CompanyResponse;
import com.ticket.support_management_system_api.features.company.service.CompanyService;
import com.ticket.support_management_system_api.features.project.dto.ProjectResponse;
import com.ticket.support_management_system_api.features.project.service.ProjectService;
import com.ticket.support_management_system_api.features.user.dto.UserResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PERM_manageCompanyAccess')")
public class CompanyController {

    private final CompanyService companyService;
    private final ProjectService projectService;
    private final ReauthenticationService reauthenticationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponse>>> findAll(
            @AuthenticationPrincipal JwtPrincipal user,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(ApiResponse.success(companyService.findAll(keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(companyService.findById(id)));
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> findUsersByCompanyId(
            @PathVariable UUID id,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(companyService.findUsersByCompanyId(id, keyword, pageable)));
    }

    @GetMapping("/{id}/projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> findProjectsByCompanyId(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findByCompanyId(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("สร้างข้อมูลบริษัทสำเร็จ", companyService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("อัปเดตข้อมูลบริษัทสำเร็จ", companyService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @Valid @RequestBody DeleteConfirmationRequest body,
            @AuthenticationPrincipal JwtPrincipal user,
            HttpServletRequest request) {
        reauthenticationService.verifyPassword(user.userId(), body.getPassword(), request);
        companyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("ลบข้อมูลบริษัทสำเร็จ", null));
    }
}
