package com.ticket.support_management_system_api.features.project.service;

import com.ticket.support_management_system_api.common.exception.DuplicateResourceException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.notification.enums.NotificationType;
import com.ticket.support_management_system_api.features.notification.service.NotificationEventPublisher;
import java.util.Map;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.company.entities.Company;
import com.ticket.support_management_system_api.features.company.repository.CompanyRepository;
import com.ticket.support_management_system_api.features.project.dto.ProjectMemberSummaryResponse;
import com.ticket.support_management_system_api.features.project.dto.ProjectRequest;
import com.ticket.support_management_system_api.features.project.dto.ProjectResponse;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.enums.ProjectMemberRole;
import com.ticket.support_management_system_api.features.project.repository.ProjectDocumentRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectMemberRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.user.repository.CustomerDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private static final UUID NO_MATCH_ID = new UUID(0L, 0L);

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectDocumentRepository documentRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> findAll(int page, int size) {
        return PaginationUtils.toPageResponse(
                projectRepository.findAllByArchivedAtIsNullOrderByCreatedAtDesc(PageRequest.of(page, size)),
                this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> findMy(int page, int size, JwtPrincipal user) {
        if (user.accountType() != AccountType.CUSTOMER) {
            return findAll(page, size);
        }
        UUID companyId = customerCompanyId(user.userId());
        List<UUID> memberProjectIds = nonEmpty(memberRepository.findProjectIdByUserIdAndArchivedAtIsNull(user.userId()));
        return PaginationUtils.toPageResponse(
                projectRepository.findAllVisibleToCustomer(companyId, memberProjectIds, PageRequest.of(page, size)),
                this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public ProjectResponse findMyById(UUID id, JwtPrincipal user) {
        Project project = getOrThrow(id);
        if (user.accountType() == AccountType.CUSTOMER) {
            UUID companyId = customerCompanyId(user.userId());
            List<UUID> memberProjectIds = nonEmpty(memberRepository.findProjectIdByUserIdAndArchivedAtIsNull(user.userId()));
            if (!projectRepository.existsVisibleToCustomer(id, companyId, memberProjectIds)) {
                throw new ResourceNotFoundException("ไม่พบโปรเจค id: " + id);
            }
        }
        return toResponse(project);
    }

    private UUID customerCompanyId(UUID userId) {
        return customerDetailsRepository.findByUserId(userId)
                .map(cd -> cd.getCompany().getId())
                .orElse(NO_MATCH_ID);
    }

    private List<UUID> nonEmpty(List<UUID> ids) {
        return ids.isEmpty() ? List.of(NO_MATCH_ID) : ids;
    }

    public ProjectResponse create(ProjectRequest request) {
        if (projectRepository.existsByNameAndArchivedAtIsNull(request.getName())) {
            throw new DuplicateResourceException("ชื่อโครงการ '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลบริษัท id: " + request.getCompanyId()));

        Project project = Project.builder()
                .name(request.getName())
                .color(request.getColor())
                .company(company)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = getOrThrow(id);
        if (projectRepository.existsByNameAndArchivedAtIsNullAndIdNot(request.getName(), id)) {
            throw new DuplicateResourceException("ชื่อโครงการ '" + request.getName() + "' มีอยู่ในระบบแล้ว");
        }
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลบริษัท id: " + request.getCompanyId()));

        project.setName(request.getName());
        project.setColor(request.getColor());
        project.setCompany(company);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        ProjectResponse response = toResponse(projectRepository.save(project));
        notificationEventPublisher.publishProjectEvent(
                NotificationType.PROJECT_UPDATED, id, null,
                "อัพเดตโปรเจค: " + project.getName(),
                "ข้อมูลโปรเจคมีการเปลี่ยนแปลง",
                Map.of("projectName", project.getName()));
        return response;
    }

    public void delete(UUID id, UUID userId) {
        Project project = getOrThrow(id);
        project.setArchivedAt(LocalDateTime.now());
        project.setArchivedBy(userId);
        projectRepository.save(project);
    }

    private Project getOrThrow(UUID id) {
        return projectRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบโปรเจค id: " + id));
    }

    private ProjectResponse toResponse(Project project) {
        long customerCount = memberRepository.countByProjectIdAndRoleAndArchivedAtIsNull(project.getId(), ProjectMemberRole.CUSTOMER);
        long assigneeCount = memberRepository.countByProjectIdAndRoleAndArchivedAtIsNull(project.getId(), ProjectMemberRole.ASSIGNEE);
        long documentCount = documentRepository.countByProjectIdAndArchivedAtIsNull(project.getId());

        List<ProjectMemberSummaryResponse> members = memberRepository
                .findAllByProjectIdAndArchivedAtIsNull(project.getId())
                .stream()
                .map(m -> ProjectMemberSummaryResponse.builder()
                        .id(m.getUser().getId())
                        .fullName(m.getUser().getFirstName() + " " + m.getUser().getLastName())
                        .profileImageUrl(m.getUser().getProfileImageUrl())
                        .build())
                .toList();

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .color(project.getColor())
                .companyId(project.getCompany().getId())
                .companyName(project.getCompany().getName())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .totalMembers(customerCount + assigneeCount)
                .customerCount(customerCount)
                .assigneeCount(assigneeCount)
                .documentCount(documentCount)
                .members(members)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
