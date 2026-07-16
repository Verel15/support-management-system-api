package com.ticket.support_management_system_api.features.report.service;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.exception.BadRequestException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.company.entities.Company;
import com.ticket.support_management_system_api.features.company.repository.CompanyRepository;
import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;
import com.ticket.support_management_system_api.features.priority.repository.PriorityRepository;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.report.dto.PdfExportContext;
import com.ticket.support_management_system_api.features.report.dto.ReportExportFormat;
import com.ticket.support_management_system_api.features.report.dto.ReportExportRequest;
import com.ticket.support_management_system_api.features.report.dto.ReportField;
import com.ticket.support_management_system_api.features.report.dto.ReportFilterRequest;
import com.ticket.support_management_system_api.features.report.dto.ReportSummaryResponse;
import com.ticket.support_management_system_api.features.report.dto.ReportTicketRow;
import com.ticket.support_management_system_api.features.report.entities.ReportExportCounter;
import com.ticket.support_management_system_api.features.report.repository.ReportExportCounterRepository;
import com.ticket.support_management_system_api.features.report.service.export.ExcelReportExporter;
import com.ticket.support_management_system_api.features.report.service.export.PdfReportExporter;
import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.entities.TicketAssignee;
import com.ticket.support_management_system_api.features.ticket.repository.TicketAssigneeRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketStatusLogRepository;
import com.ticket.support_management_system_api.features.user.entities.CustomerDetails;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.CustomerDetailsRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final List<EStatusGroup> CLOSED_STATUS_GROUPS = List.of(EStatusGroup.SUCCESS, EStatusGroup.FAILED);
    private static final DateTimeFormatter DAY_KEY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TicketRepository ticketRepository;
    private final TicketAssigneeRepository ticketAssigneeRepository;
    private final TicketStatusLogRepository ticketStatusLogRepository;
    private final CustomerDetailsRepository customerDetailsRepository;
    private final CompanyRepository companyRepository;
    private final ProjectRepository projectRepository;
    private final PriorityRepository priorityRepository;
    private final UserRepository userRepository;
    private final ReportExportCounterRepository reportExportCounterRepository;
    private final ExcelReportExporter excelReportExporter;
    private final PdfReportExporter pdfReportExporter;

    public ReportSummaryResponse getSummary(ReportFilterRequest filter, JwtPrincipal user) {
        List<Ticket> tickets = ticketRepository.findAll(buildSpec(filter, resolveCompanyIds(null, user)));
        Map<UUID, LocalDateTime> closedAtByTicketId = fetchClosedAtMap(tickets.stream().map(Ticket::getId).toList());

        long total = tickets.size();
        LocalDateTime now = LocalDateTime.now();

        long overdue = tickets.stream()
                .filter(t -> t.getDueDate() != null
                        && !CLOSED_STATUS_GROUPS.contains(t.getCurrentStatus().getGroup())
                        && t.getDueDate().isBefore(now))
                .count();

        List<Double> resolutionHoursList = new ArrayList<>();
        long resolved = 0;
        long withinSla = 0;
        for (Ticket t : tickets) {
            LocalDateTime closedAt = closedAtByTicketId.get(t.getId());
            if (closedAt == null) {
                continue;
            }
            resolved++;
            resolutionHoursList.add(Duration.between(t.getCreatedAt(), closedAt).toMinutes() / 60.0);
            if (t.getDueDate() == null || !closedAt.isAfter(t.getDueDate())) {
                withinSla++;
            }
        }

        double avgResolutionHours = resolutionHoursList.isEmpty()
                ? 0.0
                : resolutionHoursList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double slaCompliancePercent = resolved == 0 ? 0.0 : (withinSla * 100.0) / resolved;

        return ReportSummaryResponse.builder()
                .totalTickets(total)
                .resolvedTickets(resolved)
                .overdueTickets(overdue)
                .avgResolutionHours(round2(avgResolutionHours))
                .slaCompliancePercent(round2(slaCompliancePercent))
                .build();
    }

    public PageResponse<ReportTicketRow> getTickets(ReportFilterRequest filter, Pageable pageable, JwtPrincipal user) {
        var page = ticketRepository.findAll(buildSpec(filter, resolveCompanyIds(null, user)), pageable);
        List<Ticket> content = page.getContent();

        Map<UUID, List<TicketAssignee>> assigneeMap = groupAssignees(content);
        Map<UUID, LocalDateTime> closedAtByTicketId = fetchClosedAtMap(content.stream().map(Ticket::getId).toList());

        return PaginationUtils.toPageResponse(page,
                t -> toRow(t, assigneeMap.getOrDefault(t.getId(), List.of()), closedAtByTicketId.get(t.getId())));
    }

    @Transactional
    public byte[] export(ReportExportRequest request, JwtPrincipal user) {
        List<UUID> effectiveCompanyIds = resolveCompanyIds(request.getCompanyIds(), user);
        List<UUID> effectiveProjectIds = request.getProjectIds();

        Specification<Ticket> spec = buildSpec(request.getFilter(), effectiveCompanyIds);
        if (effectiveProjectIds != null && !effectiveProjectIds.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("project").get("id").in(effectiveProjectIds));
        }

        List<Ticket> tickets = ticketRepository.findAll(spec);
        Map<UUID, List<TicketAssignee>> assigneeMap = groupAssignees(tickets);
        Map<UUID, LocalDateTime> closedAtByTicketId = fetchClosedAtMap(tickets.stream().map(Ticket::getId).toList());

        List<ReportTicketRow> rows = tickets.stream()
                .map(t -> toRow(t, assigneeMap.getOrDefault(t.getId(), List.of()), closedAtByTicketId.get(t.getId())))
                .toList();

        List<ReportField> fields = request.getFields();
        if (request.getFormat() == ReportExportFormat.excel) {
            return excelReportExporter.export(rows, fields);
        }

        PdfExportContext context = buildPdfContext(request, effectiveCompanyIds, effectiveProjectIds, rows, user);
        return pdfReportExporter.export(rows, fields, context);
    }

    private List<UUID> resolveCompanyIds(List<UUID> requestedCompanyIds, JwtPrincipal user) {
        if (user.accountType() == AccountType.CUSTOMER) {
            CustomerDetails details = customerDetailsRepository.findByUserId(user.userId())
                    .orElseThrow(() -> new BadRequestException("ไม่พบข้อมูลบริษัทของผู้ใช้"));
            return List.of(details.getCompany().getId());
        }
        return requestedCompanyIds;
    }

    private Map<UUID, List<TicketAssignee>> groupAssignees(List<Ticket> tickets) {
        List<UUID> ticketIds = tickets.stream().map(Ticket::getId).toList();
        if (ticketIds.isEmpty()) {
            return Map.of();
        }
        return ticketAssigneeRepository.findAllByTicketIdInAndArchivedAtIsNull(ticketIds)
                .stream()
                .collect(Collectors.groupingBy(a -> a.getTicket().getId()));
    }

    private Map<UUID, LocalDateTime> fetchClosedAtMap(List<UUID> ticketIds) {
        if (ticketIds.isEmpty()) {
            return Map.of();
        }
        return ticketStatusLogRepository.findLastClosedAtByTicketIdIn(ticketIds)
                .stream()
                .collect(Collectors.toMap(
                        TicketStatusLogRepository.TicketClosedAt::getTicketId,
                        TicketStatusLogRepository.TicketClosedAt::getClosedAt));
    }

    private ReportTicketRow toRow(Ticket ticket, List<TicketAssignee> assignees, LocalDateTime closedAt) {
        List<UUID> assigneeIds = assignees.stream().map(a -> a.getUser().getId()).toList();
        String assigneesDisplay = assignees.stream()
                .map(a -> a.getUser().getFirstName() + " " + a.getUser().getLastName())
                .collect(Collectors.joining(", "));

        LocalDateTime now = LocalDateTime.now();
        boolean overdue = ticket.getDueDate() != null
                && !CLOSED_STATUS_GROUPS.contains(ticket.getCurrentStatus().getGroup())
                && ticket.getDueDate().isBefore(now);

        Double resolutionHours = closedAt == null
                ? null
                : round2(Duration.between(ticket.getCreatedAt(), closedAt).toMinutes() / 60.0);

        return ReportTicketRow.builder()
                .id(ticket.getId())
                .ticketId(ticket.getTicketNo())
                .title(ticket.getTitle())
                .companyId(ticket.getProject().getCompany().getId())
                .companyName(ticket.getProject().getCompany().getName())
                .projectId(ticket.getProject().getId())
                .projectName(ticket.getProject().getName())
                .assigneeIds(assigneeIds)
                .assigneesDisplay(assigneesDisplay)
                .priorityName(ticket.getSubCategory().getPriorityLevel().getName())
                .categoryName(ticket.getSubCategory().getCategory() == null ? "อื่น ๆ" : ticket.getSubCategory().getCategory().getName())
                .currentStatusName(ticket.getCurrentStatus().getName())
                .currentStatusGroup(ticket.getCurrentStatus().getGroup())
                .createdAt(ticket.getCreatedAt())
                .resolvedAt(closedAt)
                .resolutionHours(resolutionHours)
                .overdue(overdue)
                .build();
    }

    private Specification<Ticket> buildSpec(ReportFilterRequest filter, List<UUID> companyIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("archivedAt")));

            if (companyIds != null && !companyIds.isEmpty()) {
                predicates.add(root.get("project").get("company").get("id").in(companyIds));
            }
            if (filter.getProjectIds() != null && !filter.getProjectIds().isEmpty()) {
                predicates.add(root.get("project").get("id").in(filter.getProjectIds()));
            }
            if (filter.getPriorityId() != null) {
                predicates.add(cb.equal(root.get("subCategory").get("priorityLevel").get("id"), filter.getPriorityId()));
            }
            if (filter.getStatusGroup() != null) {
                predicates.add(cb.equal(root.get("currentStatus").get("group"), filter.getStatusGroup()));
            }
            if (filter.getAssigneeId() != null) {
                var assigneeSub = query.subquery(UUID.class);
                var assigneeRoot = assigneeSub.from(TicketAssignee.class);
                assigneeSub.select(assigneeRoot.get("ticket").get("id"))
                        .where(cb.equal(assigneeRoot.get("user").get("id"), filter.getAssigneeId()),
                                cb.isNull(assigneeRoot.get("archivedAt")));
                predicates.add(root.get("id").in(assigneeSub));
            }
            if (filter.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), toLocalDateTime(filter.getDateFrom())));
            }
            if (filter.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toLocalDateTime(filter.getDateTo())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PdfExportContext buildPdfContext(ReportExportRequest request, List<UUID> companyIds,
                                              List<UUID> projectIds, List<ReportTicketRow> rows, JwtPrincipal user) {
        ReportFilterRequest filter = request.getFilter();

        User exporter = userRepository.findById(user.userId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลผู้ใช้ id: " + user.userId()));

        long processing = rows.stream().filter(r -> r.getCurrentStatusGroup() == EStatusGroup.PROCESS).count();
        long pending = rows.stream().filter(r -> r.getCurrentStatusGroup() == EStatusGroup.START).count();
        long closed = rows.stream().filter(r -> CLOSED_STATUS_GROUPS.contains(r.getCurrentStatusGroup())).count();

        List<Double> resolutionHoursList = rows.stream()
                .map(ReportTicketRow::getResolutionHours)
                .filter(java.util.Objects::nonNull)
                .toList();
        double avgResolutionHours = resolutionHoursList.isEmpty()
                ? 0.0
                : resolutionHoursList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        long resolvedCount = resolutionHoursList.size();
        long withinSla = rows.stream()
                .filter(r -> r.getResolvedAt() != null && !r.isOverdue())
                .count();
        double slaCompliancePercent = resolvedCount == 0 ? 0.0 : (withinSla * 100.0) / resolvedCount;

        List<PdfExportContext.CategoryCount> categoryCounts = rows.stream()
                .collect(Collectors.groupingBy(ReportTicketRow::getCategoryName, Collectors.counting()))
                .entrySet().stream()
                .map(e -> PdfExportContext.CategoryCount.builder().name(e.getKey()).count(e.getValue()).build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .toList();

        return PdfExportContext.builder()
                .documentNo(nextDocumentNo())
                .exportedAt(LocalDateTime.now())
                .exporterName(exporter.getFirstName() + " " + exporter.getLastName())
                .companyLabel(companyLabel(companyIds))
                .projectLabel(projectLabel(projectIds))
                .dateRangeLabel(dateRangeLabel(filter.getDateFrom(), filter.getDateTo()))
                .assigneeLabel(assigneeLabel(filter.getAssigneeId()))
                .priorityLabel(priorityLabel(filter.getPriorityId()))
                .statusLabel(statusLabel(filter.getStatusGroup()))
                .totalTickets(rows.size())
                .closedTickets(closed)
                .processingTickets(processing)
                .pendingTickets(pending)
                .avgResolutionHours(round2(avgResolutionHours))
                .slaCompliancePercent(round2(slaCompliancePercent))
                .categoryCounts(categoryCounts)
                .build();
    }

    private String nextDocumentNo() {
        String dayKey = LocalDateTime.now().format(DAY_KEY_FORMAT);
        ReportExportCounter counter = reportExportCounterRepository.findByDayKey(dayKey)
                .orElseGet(() -> reportExportCounterRepository.save(new ReportExportCounter(dayKey, 0)));
        counter.setLastSeq(counter.getLastSeq() + 1);
        reportExportCounterRepository.save(counter);
        return String.format("SMS-EXP-%s-%s-%03d", dayKey.substring(0, 4), dayKey.substring(4), counter.getLastSeq());
    }

    private String companyLabel(List<UUID> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) {
            return "ทุกบริษัท";
        }
        List<Company> companies = companyRepository.findAllById(companyIds);
        if (companies.size() == 1) {
            return "บริษัท " + companies.get(0).getName();
        }
        return companies.stream().map(Company::getName).collect(Collectors.joining(", "));
    }

    private String projectLabel(List<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return "ทุกโครงการ";
        }
        List<Project> projects = projectRepository.findAllById(projectIds);
        return projects.stream().map(Project::getName).collect(Collectors.joining(", "));
    }

    private String dateRangeLabel(Instant dateFrom, Instant dateTo) {
        if (dateFrom == null && dateTo == null) {
            return "ทุกช่วงวันที่";
        }
        String from = dateFrom == null ? "เริ่มต้น" : toLocalDateTime(dateFrom).format(DATE_LABEL_FORMAT);
        String to = dateTo == null ? "ปัจจุบัน" : toLocalDateTime(dateTo).format(DATE_LABEL_FORMAT);
        return from + " - " + to;
    }

    private String assigneeLabel(UUID assigneeId) {
        if (assigneeId == null) {
            return "ผู้รับผิดชอบทั้งหมด";
        }
        return userRepository.findById(assigneeId)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("ผู้รับผิดชอบทั้งหมด");
    }

    private String priorityLabel(UUID priorityId) {
        if (priorityId == null) {
            return "ลำดับความสำคัญทั้งหมด";
        }
        return priorityRepository.findByIdAndArchivedAtIsNull(priorityId)
                .map(PriorityLevels::getName)
                .orElse("ลำดับความสำคัญทั้งหมด");
    }

    private String statusLabel(EStatusGroup statusGroup) {
        if (statusGroup == null) {
            return "สถานะทั้งหมด";
        }
        return switch (statusGroup) {
            case START -> "รอตรวจสอบ";
            case PROCESS -> "กำลังดำเนินการ";
            case SUCCESS -> "สำเร็จ";
            case FAILED -> "ไม่สำเร็จ";
        };
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
