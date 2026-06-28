package com.ticket.support_management_system_api.features.project.controller;

import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.features.project.dto.ProjectMemberRequest;
import com.ticket.support_management_system_api.features.project.dto.ProjectMemberResponse;
import com.ticket.support_management_system_api.features.project.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> findAll(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.findAllByProject(projectId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectMemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("เพิ่มสมาชิกสำเร็จ", memberService.addMember(projectId, request)));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID memberId) {
        memberService.removeMember(projectId, memberId);
        return ResponseEntity.ok(ApiResponse.success("ลบสมาชิกสำเร็จ", null));
    }
}
