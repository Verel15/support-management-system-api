package com.ticket.support_management_system_api.features.project.service;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.company.entities.Company;
import com.ticket.support_management_system_api.features.company.repository.CompanyRepository;
import com.ticket.support_management_system_api.features.notification.service.NotificationEventPublisher;
import com.ticket.support_management_system_api.features.project.dto.ProjectFilterRequest;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.repository.ProjectDocumentRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectMemberRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.ticket.service.TicketService;
import com.ticket.support_management_system_api.features.user.entities.CustomerDetails;
import com.ticket.support_management_system_api.features.user.repository.CustomerDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ProjectMemberRepository memberRepository;
    @Mock
    private ProjectDocumentRepository documentRepository;
    @Mock
    private CustomerDetailsRepository customerDetailsRepository;
    @Mock
    private NotificationEventPublisher notificationEventPublisher;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketService ticketService;

    @InjectMocks
    private ProjectService projectService;

    private UUID customerUserId;
    private UUID companyId;
    private Project project;

    @BeforeEach
    void setUp() {
        customerUserId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        Company company = Company.builder().name("acme").build();
        company.setId(companyId);

        project = Project.builder()
                .name("proj")
                .company(company)
                .startDate(java.time.LocalDate.now().minusDays(1))
                .endDate(java.time.LocalDate.now().plusDays(1))
                .build();
        project.setId(UUID.randomUUID());
    }

    private void stubProjectResponseLookups() {
        when(memberRepository.countByProjectIdAndRoleAndArchivedAtIsNull(any(), any())).thenReturn(0L);
        when(documentRepository.countByProjectIdAndArchivedAtIsNull(any())).thenReturn(0L);
        when(memberRepository.findAllByProjectIdAndArchivedAtIsNull(any())).thenReturn(List.of());
        when(ticketRepository.countByProjectIdAndArchivedAtIsNull(any())).thenReturn(0L);
        when(ticketRepository.countByProjectIdAndCurrentStatus_GroupAndArchivedAtIsNull(any(), any())).thenReturn(0L);
    }

    @Test
    void findById_returnsProject_regardlessOfCaller() {
        stubProjectResponseLookups();
        when(projectRepository.findByIdAndArchivedAtIsNull(project.getId())).thenReturn(Optional.of(project));

        var result = projectService.findById(project.getId());

        assertThat(result.getId()).isEqualTo(project.getId());
        verify(customerDetailsRepository, never()).findByUserId(any());
    }

    @Test
    void findAll_usesUnscopedQuery() {
        stubProjectResponseLookups();
        Page<Project> page = new PageImpl<>(List.of(project));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        var result = projectService.findAll(new ProjectFilterRequest(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findMyById_customerInProjectCompany_returnsProject() {
        stubProjectResponseLookups();
        when(projectRepository.findByIdAndArchivedAtIsNull(project.getId())).thenReturn(Optional.of(project));
        when(customerDetailsRepository.findByUserId(customerUserId))
                .thenReturn(Optional.of(customerDetailsFor(companyId)));
        when(memberRepository.findProjectIdByUserIdAndArchivedAtIsNull(customerUserId)).thenReturn(List.of());
        when(projectRepository.existsVisibleToCustomer(eq(project.getId()), eq(companyId), anyList())).thenReturn(true);

        JwtPrincipal customer = new JwtPrincipal(customerUserId, "a@b.com", AccountType.CUSTOMER, UUID.randomUUID(), List.of());

        var result = projectService.findMyById(project.getId(), customer);

        assertThat(result.getId()).isEqualTo(project.getId());
    }

    @Test
    void findMyById_customerOutsideScope_throwsNotFound() {
        when(projectRepository.findByIdAndArchivedAtIsNull(project.getId())).thenReturn(Optional.of(project));
        when(customerDetailsRepository.findByUserId(customerUserId))
                .thenReturn(Optional.of(customerDetailsFor(UUID.randomUUID())));
        when(memberRepository.findProjectIdByUserIdAndArchivedAtIsNull(customerUserId)).thenReturn(List.of());
        when(projectRepository.existsVisibleToCustomer(eq(project.getId()), any(), anyList())).thenReturn(false);

        JwtPrincipal customer = new JwtPrincipal(customerUserId, "a@b.com", AccountType.CUSTOMER, UUID.randomUUID(), List.of());

        assertThatThrownBy(() -> projectService.findMyById(project.getId(), customer))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findMyById_externalUser_bypassesScopeCheck() {
        stubProjectResponseLookups();
        when(projectRepository.findByIdAndArchivedAtIsNull(project.getId())).thenReturn(Optional.of(project));

        JwtPrincipal staff = new JwtPrincipal(UUID.randomUUID(), "a@b.com", AccountType.STAFF, UUID.randomUUID(), List.of());

        var result = projectService.findMyById(project.getId(), staff);

        assertThat(result.getId()).isEqualTo(project.getId());
        verify(customerDetailsRepository, never()).findByUserId(any());
    }

    @Test
    void findMy_customer_usesScopedQuery() {
        stubProjectResponseLookups();
        when(customerDetailsRepository.findByUserId(customerUserId))
                .thenReturn(Optional.of(customerDetailsFor(companyId)));
        when(memberRepository.findProjectIdByUserIdAndArchivedAtIsNull(customerUserId)).thenReturn(List.of());
        Page<Project> page = new PageImpl<>(List.of(project));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        JwtPrincipal customer = new JwtPrincipal(customerUserId, "a@b.com", AccountType.CUSTOMER, UUID.randomUUID(), List.of());

        var result = projectService.findMy(new ProjectFilterRequest(), PageRequest.of(0, 10), customer);

        assertThat(result.getContent()).hasSize(1);
        verify(projectRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findMy_externalUser_usesUnscopedQuery() {
        stubProjectResponseLookups();
        Page<Project> page = new PageImpl<>(List.of(project));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        JwtPrincipal staff = new JwtPrincipal(UUID.randomUUID(), "a@b.com", AccountType.STAFF, UUID.randomUUID(), List.of());

        var result = projectService.findMy(new ProjectFilterRequest(), PageRequest.of(0, 10), staff);

        assertThat(result.getContent()).hasSize(1);
        verify(customerDetailsRepository, never()).findByUserId(any());
    }

    private CustomerDetails customerDetailsFor(UUID companyId) {
        Company company = Company.builder().name("c").build();
        company.setId(companyId);
        return CustomerDetails.builder().company(company).build();
    }
}
