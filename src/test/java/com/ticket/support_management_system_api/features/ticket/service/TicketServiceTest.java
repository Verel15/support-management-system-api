package com.ticket.support_management_system_api.features.ticket.service;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.notification.service.NotificationEventPublisher;
import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.status.entities.StatusFlows;
import com.ticket.support_management_system_api.features.status.entities.Statuses;
import com.ticket.support_management_system_api.features.status.repository.StatusRepository;
import com.ticket.support_management_system_api.features.ticket.dto.TicketFilterRequest;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.repository.TicketAssigneeRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketStatusLogRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketYearCounterRepository;
import com.ticket.support_management_system_api.features.ticket_sub_category.entities.TicketSubCategory;
import com.ticket.support_management_system_api.features.ticket_sub_category.repository.TicketSubCategoryRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TicketSubCategoryRepository subCategoryRepository;
    @Mock
    private StatusRepository statusRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketAssigneeRepository ticketAssigneeRepository;
    @Mock
    private TicketStatusLogRepository statusLogRepository;
    @Mock
    private TicketYearCounterRepository yearCounterRepository;
    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @InjectMocks
    private TicketService ticketService;

    private UUID requesterId;
    private UUID otherUserId;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        requesterId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        User requester = User.builder().build();
        requester.setId(requesterId);

        Project project = Project.builder().name("proj").build();
        project.setId(UUID.randomUUID());

        TicketSubCategory subCategory = TicketSubCategory.builder()
                .priorityLevel(PriorityLevels.builder().build())
                .build();
        subCategory.setId(UUID.randomUUID());

        StatusFlows flow = StatusFlows.builder().build();
        flow.setId(UUID.randomUUID());

        Statuses status = Statuses.builder().build();
        status.setId(UUID.randomUUID());

        ticket = Ticket.builder()
                .title("t")
                .ticketYear(2026)
                .ticketSeq(1)
                .project(project)
                .subCategory(subCategory)
                .currentStatus(status)
                .statusFlow(flow)
                .requester(requester)
                .build();
        ticket.setId(UUID.randomUUID());
    }

    @Test
    void findById_returnsTicket_regardlessOfCaller() {
        when(ticketRepository.findByIdAndArchivedAtIsNull(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(ticket.getId())).thenReturn(List.of());

        var result = ticketService.findById(ticket.getId());

        assertThat(result.getId()).isEqualTo(ticket.getId());
    }

    @Test
    void findAll_delegatesToRepositoryWithoutUserScoping() {
        Page<Ticket> page = new PageImpl<>(List.of(ticket));
        when(ticketRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(ticketAssigneeRepository.findAllByTicketIdInAndArchivedAtIsNull(any())).thenReturn(List.of());

        var result = ticketService.findAll(new TicketFilterRequest(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findMyById_customerOwnTicket_returnsTicket() {
        when(ticketRepository.findByIdAndArchivedAtIsNull(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(ticket.getId())).thenReturn(List.of());

        JwtPrincipal customer = new JwtPrincipal(requesterId, "a@b.com", AccountType.CUSTOMER, UUID.randomUUID(), List.of());

        var result = ticketService.findMyById(ticket.getId(), customer);

        assertThat(result.getId()).isEqualTo(ticket.getId());
    }

    @Test
    void findMyById_customerOtherUsersTicket_throwsNotFound() {
        when(ticketRepository.findByIdAndArchivedAtIsNull(ticket.getId())).thenReturn(Optional.of(ticket));

        JwtPrincipal customer = new JwtPrincipal(otherUserId, "a@b.com", AccountType.CUSTOMER, UUID.randomUUID(), List.of());

        assertThatThrownBy(() -> ticketService.findMyById(ticket.getId(), customer))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findMyById_externalUser_seesAnyTicket() {
        when(ticketRepository.findByIdAndArchivedAtIsNull(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(ticket.getId())).thenReturn(List.of());

        JwtPrincipal staff = new JwtPrincipal(otherUserId, "a@b.com", AccountType.EXTERNAL, UUID.randomUUID(), List.of());

        var result = ticketService.findMyById(ticket.getId(), staff);

        assertThat(result.getId()).isEqualTo(ticket.getId());
    }

    @Test
    void findMy_delegatesToRepositoryWithSpecification() {
        Page<Ticket> page = new PageImpl<>(List.of(ticket));
        when(ticketRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(ticketAssigneeRepository.findAllByTicketIdInAndArchivedAtIsNull(any())).thenReturn(List.of());

        JwtPrincipal customer = new JwtPrincipal(requesterId, "a@b.com", AccountType.CUSTOMER, UUID.randomUUID(), List.of());

        var result = ticketService.findMy(new TicketFilterRequest(), PageRequest.of(0, 10), customer);

        assertThat(result.getContent()).hasSize(1);
    }
}
