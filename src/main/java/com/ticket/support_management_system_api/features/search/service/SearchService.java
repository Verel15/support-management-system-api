package com.ticket.support_management_system_api.features.search.service;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.exception.BadRequestException;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.repository.ProjectMemberRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.search.dto.SearchResult;
import com.ticket.support_management_system_api.features.search.dto.SearchResultType;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.user.entities.CustomerDetails;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.CustomerDetailsRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public List<SearchResult> search(String q, List<SearchResultType> types, int limit, JwtPrincipal user) {
        if (q == null || q.isBlank()) {
            throw new BadRequestException("กรุณาระบุคำค้นหา");
        }
        String keyword = q.trim();
        Pageable pageable = PageRequest.of(0, limit);

        List<SearchResult> results = new ArrayList<>();
        if (types.contains(SearchResultType.TICKET)) {
            results.addAll(searchTickets(keyword, pageable, user));
        }
        if (types.contains(SearchResultType.PROJECT)) {
            results.addAll(searchProjects(keyword, pageable, user));
        }
        if (types.contains(SearchResultType.USER)) {
            results.addAll(searchUsers(keyword, pageable, user));
        }
        return results;
    }

    private UUID resolveCustomerCompanyId(JwtPrincipal user) {
        CustomerDetails details = customerDetailsRepository.findByUserId(user.userId())
                .orElseThrow(() -> new BadRequestException("ไม่พบข้อมูลบริษัทของผู้ใช้"));
        return details.getCompany().getId();
    }

    private List<UUID> resolveStaffProjectIds(JwtPrincipal user) {
        return projectMemberRepository.findProjectIdByUserIdAndArchivedAtIsNull(user.userId());
    }

    private List<SearchResult> searchTickets(String keyword, Pageable pageable, JwtPrincipal user) {
        List<Ticket> tickets = switch (user.accountType()) {
            case ADMIN -> ticketRepository.searchByTitle(keyword, pageable);
            case CUSTOMER -> ticketRepository.searchByTitleAndRequesterId(keyword, user.userId(), pageable);
            case STAFF -> ticketRepository.searchByTitleForStaff(keyword, user.userId(), resolveStaffProjectIds(user), pageable);
        };

        return tickets.stream().map(t -> SearchResult.builder()
                .type(SearchResultType.TICKET)
                .id(t.getId().toString())
                .title(t.getTitle())
                .subtitle(t.getTicketNo() + " · " + t.getProject().getName() + " · " + t.getCurrentStatus().getName())
                .meta(t.getCreatedAt() == null ? null : t.getCreatedAt().format(DATE_FORMAT))
                .build())
                .toList();
    }

    private List<SearchResult> searchProjects(String keyword, Pageable pageable, JwtPrincipal user) {
        List<Project> projects = switch (user.accountType()) {
            case ADMIN -> projectRepository.searchByName(keyword, pageable);
            case CUSTOMER -> projectRepository.searchByNameAndCompanyId(keyword, resolveCustomerCompanyId(user), pageable);
            case STAFF -> projectRepository.searchByNameAndProjectIds(keyword, resolveStaffProjectIds(user), pageable);
        };

        return projects.stream().map(p -> {
            long ticketCount = ticketRepository.countByProjectIdAndArchivedAtIsNull(p.getId());
            return SearchResult.builder()
                    .type(SearchResultType.PROJECT)
                    .id(p.getId().toString())
                    .title(p.getName())
                    .subtitle(p.getCompany().getName())
                    .meta(ticketCount + " ticket")
                    .build();
        }).toList();
    }

    private List<SearchResult> searchUsers(String keyword, Pageable pageable, JwtPrincipal user) {
        List<User> users = switch (user.accountType()) {
            case ADMIN -> userRepository.searchByName(keyword, pageable);
            case CUSTOMER -> userRepository.searchByNameAndCompanyId(keyword, resolveCustomerCompanyId(user), pageable);
            case STAFF -> userRepository.searchByNameAndProjectIds(keyword, resolveStaffProjectIds(user), pageable);
        };

        return users.stream().map(u -> SearchResult.builder()
                .type(SearchResultType.USER)
                .id(u.getId().toString())
                .title(u.getFirstName() + " " + u.getLastName())
                .subtitle(u.getEmail())
                .meta(u.getAccountType().name())
                .build())
                .toList();
    }
}
