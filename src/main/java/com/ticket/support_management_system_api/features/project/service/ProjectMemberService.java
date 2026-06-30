package com.ticket.support_management_system_api.features.project.service;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.notification.enums.NotificationType;
import com.ticket.support_management_system_api.features.notification.service.NotificationEventPublisher;
import java.util.Map;
import com.ticket.support_management_system_api.features.project.dto.ProjectMemberRequest;
import com.ticket.support_management_system_api.features.project.dto.ProjectMemberResponse;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.entities.ProjectMember;
import com.ticket.support_management_system_api.features.project.repository.ProjectMemberRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectMemberService {

    private final ProjectMemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> findAllByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("ไม่พบโปรเจค id: " + projectId);
        }
        return memberRepository.findAllByProjectIdAndArchivedAtIsNull(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectMemberResponse addMember(UUID projectId, ProjectMemberRequest request) {
        Project project = projectRepository.findByIdAndArchivedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบโปรเจค id: " + projectId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + request.getUserId()));

        if (memberRepository.existsByProjectIdAndUserIdAndRoleAndArchivedAtIsNull(projectId, request.getUserId(), request.getRole())) {
            throw new DuplicateResourceException("ผู้ใช้นี้มีบทบาท " + request.getRole() + " ในโปรเจคนี้อยู่แล้ว");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(request.getRole())
                .build();
        ProjectMemberResponse response = toResponse(memberRepository.save(member));
        notificationEventPublisher.publishProjectEvent(
                NotificationType.PROJECT_MEMBER_ADDED, projectId, null,
                "สมาชิกใหม่ใน " + project.getName(),
                user.getFirstName() + " " + user.getLastName() + " ถูกเพิ่มเข้าโปรเจค",
                Map.of("projectName", project.getName(), "memberName", user.getFirstName() + " " + user.getLastName()));
        return response;
    }

    public void removeMember(UUID projectId, UUID memberId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("ไม่พบโปรเจค id: " + projectId);
        }
        ProjectMember member = memberRepository.findByIdAndProjectIdAndArchivedAtIsNull(memberId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบสมาชิก id: " + memberId));

        member.setArchivedAt(LocalDateTime.now());
        memberRepository.save(member);
        notificationEventPublisher.publishProjectEvent(
                NotificationType.PROJECT_MEMBER_REMOVED, projectId, null,
                "สมาชิกออกจาก " + member.getProject().getName(),
                member.getUser().getFirstName() + " " + member.getUser().getLastName() + " ถูกนำออกจากโปรเจค",
                Map.of("projectName", member.getProject().getName(),
                        "memberName", member.getUser().getFirstName() + " " + member.getUser().getLastName()));
    }

    private ProjectMemberResponse toResponse(ProjectMember member) {
        User user = member.getUser();
        return ProjectMemberResponse.builder()
                .id(member.getId())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
