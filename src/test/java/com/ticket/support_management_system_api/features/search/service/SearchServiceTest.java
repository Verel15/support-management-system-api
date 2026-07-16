package com.ticket.support_management_system_api.features.search.service;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.exception.BadRequestException;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.company.entities.Company;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.search.dto.SearchResult;
import com.ticket.support_management_system_api.features.search.dto.SearchResultType;
import com.ticket.support_management_system_api.features.status.entities.Statuses;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.user.entities.CustomerDetails;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.CustomerDetailsRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerDetailsRepository customerDetailsRepository;

    @InjectMocks
    private SearchService searchService;

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_USER_ID = UUID.randomUUID();
    private static final UUID EXTERNAL_USER_ID = UUID.randomUUID();

    @Test
    void blankKeywordThrows() {
        JwtPrincipal external = new JwtPrincipal(EXTERNAL_USER_ID, "staff@x.com", AccountType.EXTERNAL, null, List.of());
        assertThatThrownBy(() -> searchService.search("  ", List.of(SearchResultType.TICKET), 5, external))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void customerSearchIsScopedToOwnCompany() {
        JwtPrincipal customer = new JwtPrincipal(CUSTOMER_USER_ID, "customer@x.com", AccountType.CUSTOMER, null, List.of());

        Company company = Company.builder().name("บริษัท เอบีซี").build();
        company.setId(COMPANY_ID);
        CustomerDetails details = CustomerDetails.builder().userId(CUSTOMER_USER_ID).company(company).build();
        when(customerDetailsRepository.findByUserId(CUSTOMER_USER_ID)).thenReturn(Optional.of(details));

        Project project = Project.builder().name("โครงการทดสอบ").company(company).build();
        project.setId(UUID.randomUUID());
        when(projectRepository.searchByNameAndCompanyId(eq("vpn"), eq(COMPANY_ID), any())).thenReturn(List.of(project));
        when(ticketRepository.countByProjectIdAndArchivedAtIsNull(any())).thenReturn(3L);

        List<SearchResult> results = searchService.search("vpn", List.of(SearchResultType.PROJECT), 5, customer);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("โครงการทดสอบ");
        verify(projectRepository).searchByNameAndCompanyId(eq("vpn"), eq(COMPANY_ID), any());
        verify(projectRepository, never()).searchByName(anyString(), any());
    }

    @Test
    void externalSearchIsNotCompanyScoped() {
        JwtPrincipal external = new JwtPrincipal(EXTERNAL_USER_ID, "staff@x.com", AccountType.EXTERNAL, null, List.of());

        Company otherCompany = Company.builder().name("บริษัท เอ็กซ์วายแซด").build();
        otherCompany.setId(UUID.randomUUID());
        Project project = Project.builder().name("โครงการ ERP").company(otherCompany).build();
        project.setId(UUID.randomUUID());
        when(projectRepository.searchByName(eq("erp"), any())).thenReturn(List.of(project));
        when(ticketRepository.countByProjectIdAndArchivedAtIsNull(any())).thenReturn(0L);

        List<SearchResult> results = searchService.search("erp", List.of(SearchResultType.PROJECT), 5, external);

        assertThat(results).hasSize(1);
        verify(projectRepository).searchByName(eq("erp"), any());
        verify(customerDetailsRepository, never()).findByUserId(any());
    }

    @Test
    void ticketResultTitleContainsKeywordForClientSideHighlight() {
        JwtPrincipal external = new JwtPrincipal(EXTERNAL_USER_ID, "staff@x.com", AccountType.EXTERNAL, null, List.of());

        Project project = Project.builder().name("โครงการ VPN").build();
        project.setId(UUID.randomUUID());
        Statuses status = Statuses.builder().name("เปิดงาน").build();
        status.setId(UUID.randomUUID());
        Ticket ticket = Ticket.builder()
                .ticketNo("TCK-1042")
                .title("ไม่สามารถเข้าสู่ระบบ VPN บริษัทได้")
                .project(project)
                .currentStatus(status)
                .build();
        ticket.setId(UUID.randomUUID());
        ticket.setCreatedAt(LocalDateTime.now());
        when(ticketRepository.searchByTitle(eq("vpn"), any())).thenReturn(List.of(ticket));

        List<SearchResult> results = searchService.search("vpn", List.of(SearchResultType.TICKET), 5, external);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle().toLowerCase()).contains("vpn");
        assertThat(results.get(0).getId()).isEqualTo(ticket.getId().toString());
    }

    @Test
    void combinesMultipleTypesInSingleResponse() {
        JwtPrincipal external = new JwtPrincipal(EXTERNAL_USER_ID, "staff@x.com", AccountType.EXTERNAL, null, List.of());

        when(ticketRepository.searchByTitle(anyString(), any())).thenReturn(List.of());
        when(projectRepository.searchByName(anyString(), any())).thenReturn(List.of());
        User user = User.builder().firstName("กิตติ").lastName("วัฒนกุล")
                .email("kitti@x.com").accountType(AccountType.EXTERNAL).build();
        user.setId(UUID.randomUUID());
        when(userRepository.searchByName(anyString(), any())).thenReturn(List.of(user));

        List<SearchResult> results = searchService.search("กิตติ",
                List.of(SearchResultType.TICKET, SearchResultType.PROJECT, SearchResultType.USER), 5, external);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType()).isEqualTo(SearchResultType.USER);
    }
}
