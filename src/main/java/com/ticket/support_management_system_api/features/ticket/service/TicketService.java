package com.ticket.support_management_system_api.features.ticket.service;

import com.ticket.support_management_system_api.common.exception.BadRequestException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;
import com.ticket.support_management_system_api.features.priority.enums.EIntervalUnit;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.status.entities.StatusFlows;
import com.ticket.support_management_system_api.features.status.entities.Statuses;
import com.ticket.support_management_system_api.features.status.enums.StatusGroup;
import com.ticket.support_management_system_api.features.status.repository.StatusRepository;
import com.ticket.support_management_system_api.features.ticket.dto.*;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.entities.TicketAssignee;
import com.ticket.support_management_system_api.features.ticket.entities.TicketStatusLog;
import com.ticket.support_management_system_api.features.ticket.entities.TicketYearCounter;
import com.ticket.support_management_system_api.features.ticket.repository.TicketAssigneeRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketStatusLogRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketYearCounterRepository;
import com.ticket.support_management_system_api.features.ticket_sub_category.entities.TicketSubCategory;
import com.ticket.support_management_system_api.features.ticket_sub_category.repository.TicketSubCategoryRepository;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final TicketSubCategoryRepository subCategoryRepository;
    private final StatusRepository statusRepository;
    private final UserRepository userRepository;
    private final TicketAssigneeRepository ticketAssigneeRepository;
    private final TicketStatusLogRepository statusLogRepository;
    private final TicketYearCounterRepository yearCounterRepository;

    @Transactional(readOnly = true)
    public PageResponse<TicketListResponse> findAll(TicketFilterRequest filter, Pageable pageable) {
        Specification<Ticket> spec = buildSpec(filter);
        var page = ticketRepository.findAll(spec, pageable);

        List<UUID> ticketIds = page.getContent().stream().map(Ticket::getId).toList();
        Map<UUID, List<TicketAssignee>> assigneeMap = ticketIds.isEmpty()
                ? Map.of()
                : ticketAssigneeRepository.findAllByTicketIdInAndArchivedAtIsNull(ticketIds)
                        .stream()
                        .collect(Collectors.groupingBy(a -> a.getTicket().getId()));

        final Map<UUID, List<TicketAssignee>> finalAssigneeMap = assigneeMap;
        return PaginationUtils.toPageResponse(page, t -> toListResponse(t, finalAssigneeMap.getOrDefault(t.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse findById(UUID id) {
        Ticket ticket = getOrThrow(id);
        List<TicketAssignee> assignees = ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(id);
        return toDetailResponse(ticket, assignees);
    }

    public TicketDetailResponse create(CreateTicketRequest request, UUID actorUserId) {
        Project project = projectRepository.findByIdAndArchivedAtIsNull(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบโครงการ id: " + request.getProjectId()));
        TicketSubCategory subCategory = subCategoryRepository.findByIdAndArchivedAtIsNull(request.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบประเภทย่อย Ticket id: " + request.getSubCategoryId()));
        StatusFlows statusFlow = subCategory.getCategory().getStatusFlow();

        UUID requesterId = request.getRequesterId() != null ? request.getRequesterId() : actorUserId;
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + requesterId));
        User actor = actorUserId.equals(requesterId) ? requester : userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + actorUserId));

        Statuses startStatus = resolveStartStatus(statusFlow.getId());
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int seq = nextSeq(year);
        LocalDateTime dueDate = computeDueDate(now, subCategory.getPriorityLevel());

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .ticketYear(year)
                .ticketSeq(seq)
                .project(project)
                .subCategory(subCategory)
                .currentStatus(startStatus)
                .statusFlow(statusFlow)
                .dueDate(dueDate)
                .requester(requester)
                .build();
        ticket = ticketRepository.saveAndFlush(ticket);

        TicketStatusLog initialLog = TicketStatusLog.builder()
                .ticket(ticket)
                .changedBy(actor)
                .fromStatus(null)
                .toStatus(startStatus)
                .build();
        statusLogRepository.save(initialLog);

        return toDetailResponse(ticket, List.of());
    }

    public TicketDetailResponse update(UUID id, UpdateTicketRequest request, UUID actorUserId) {
        Ticket ticket = getOrThrow(id);

        Project project = projectRepository.findByIdAndArchivedAtIsNull(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบโครงการ id: " + request.getProjectId()));
        TicketSubCategory subCategory = subCategoryRepository.findByIdAndArchivedAtIsNull(request.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบประเภทย่อย Ticket id: " + request.getSubCategoryId()));
        StatusFlows statusFlow = subCategory.getCategory().getStatusFlow();
        boolean statusFlowChanged = !ticket.getStatusFlow().getId().equals(statusFlow.getId());

        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        ticket.setProject(project);
        ticket.setSubCategory(subCategory);
        ticket.setStatusFlow(statusFlow);
        ticket.setDueDate(computeDueDate(ticket.getCreatedAt(), subCategory.getPriorityLevel()));

        if (statusFlowChanged) {
            User actor = userRepository.findById(actorUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + actorUserId));
            Statuses newStartStatus = resolveStartStatus(statusFlow.getId());
            Statuses oldStatus = ticket.getCurrentStatus();
            ticket.setCurrentStatus(newStartStatus);
            TicketStatusLog log = TicketStatusLog.builder()
                    .ticket(ticket)
                    .changedBy(actor)
                    .fromStatus(oldStatus)
                    .toStatus(newStartStatus)
                    .note("เปลี่ยนทีมรับเรื่อง")
                    .build();
            statusLogRepository.save(log);
        }

        ticket = ticketRepository.save(ticket);
        List<TicketAssignee> assignees = ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(id);
        return toDetailResponse(ticket, assignees);
    }

    public void delete(UUID id, UUID actorUserId) {
        Ticket ticket = getOrThrow(id);
        ticket.setArchivedAt(LocalDateTime.now());
        ticket.setArchivedBy(actorUserId);
        ticketRepository.save(ticket);
    }

    public TicketDetailResponse changeStatus(UUID id, ChangeTicketStatusRequest request, UUID actorUserId) {
        Ticket ticket = getOrThrow(id);
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + actorUserId));

        if (!statusRepository.existsByIdAndFlowId(request.getToStatusId(), ticket.getStatusFlow().getId())) {
            throw new BadRequestException("สถานะนี้ไม่ได้อยู่ใน flow ของ Ticket นี้");
        }

        Statuses toStatus = statusRepository.findById(request.getToStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบสถานะ id: " + request.getToStatusId()));

        Statuses fromStatus = ticket.getCurrentStatus();
        ticket.setCurrentStatus(toStatus);
        ticketRepository.save(ticket);

        TicketStatusLog log = TicketStatusLog.builder()
                .ticket(ticket)
                .changedBy(actor)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .note(request.getNote())
                .build();
        statusLogRepository.save(log);

        List<TicketAssignee> assignees = ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(id);
        return toDetailResponse(ticket, assignees);
    }

    private int nextSeq(int year) {
        TicketYearCounter counter = yearCounterRepository.findByYear(year)
                .orElseGet(() -> yearCounterRepository.save(new TicketYearCounter(year, 0)));
        counter.setLastSeq(counter.getLastSeq() + 1);
        yearCounterRepository.save(counter);
        return counter.getLastSeq();
    }

    private String formatTicketId(Integer year, Integer seq) {
        if (year == null || seq == null) return "TK-???-????";
        return String.format("TK-%d-%04d", year, seq);
    }

    private Ticket getOrThrow(UUID id) {
        return ticketRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบ Ticket id: " + id));
    }

    private Statuses resolveStartStatus(UUID statusFlowId) {
        return statusRepository.findFirstByFlowIdAndGroupOrderBySequenceAsc(statusFlowId, StatusGroup.START)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบสถานะเริ่มต้นสำหรับทีมรับเรื่องนี้"));
    }

    private LocalDateTime computeDueDate(LocalDateTime from, PriorityLevels priority) {
        ChronoUnit unit = toChronoUnit(priority.getIntervalUnit());
        return from.plus(priority.getIntervalValue(), unit);
    }

    private ChronoUnit toChronoUnit(EIntervalUnit unit) {
        return switch (unit) {
            case MINUTE -> ChronoUnit.MINUTES;
            case HOUR -> ChronoUnit.HOURS;
            case DAY -> ChronoUnit.DAYS;
            case WEEK -> ChronoUnit.WEEKS;
            case MONTH -> ChronoUnit.MONTHS;
            case YEAR -> ChronoUnit.YEARS;
        };
    }

    private Specification<Ticket> buildSpec(TicketFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("archivedAt")));

            if (filter.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filter.getProjectId()));
            }
            if (filter.getStatusId() != null) {
                predicates.add(cb.equal(root.get("currentStatus").get("id"), filter.getStatusId()));
            }
            if (filter.getPriorityId() != null) {
                predicates.add(cb.equal(root.get("priority").get("id"), filter.getPriorityId()));
            }
            if (filter.getStatusFlowId() != null) {
                predicates.add(cb.equal(root.get("statusFlow").get("id"), filter.getStatusFlowId()));
            }
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), kw));
            }
            if (Boolean.TRUE.equals(filter.getOverdue())) {
                predicates.add(cb.lessThan(root.get("dueDate"), LocalDateTime.now()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private TicketListResponse toListResponse(Ticket ticket, List<TicketAssignee> assignees) {
        return TicketListResponse.builder()
                .id(ticket.getId())
                .ticketId(formatTicketId(ticket.getTicketYear(), ticket.getTicketSeq()))
                .title(ticket.getTitle())
                .projectId(ticket.getProject().getId())
                .projectName(ticket.getProject().getName())
                .currentStatusId(ticket.getCurrentStatus().getId())
                .currentStatusName(ticket.getCurrentStatus().getName())
                .currentStatusGroup(ticket.getCurrentStatus().getGroup())
                .statusFlowId(ticket.getStatusFlow().getId())
                .statusFlowName(ticket.getStatusFlow().getName())
                .priorityId(ticket.getSubCategory().getPriorityLevel().getId())
                .priorityName(ticket.getSubCategory().getPriorityLevel().getName())
                .priorityIconShape(ticket.getSubCategory().getPriorityLevel().getIconShape())
                .priorityIconColor(ticket.getSubCategory().getPriorityLevel().getIconColor())
                .dueDate(ticket.getDueDate())
                .createdAt(ticket.getCreatedAt())
                .assignees(assignees.stream().map(a -> TicketAssigneeSummary.builder()
                        .id(a.getUser().getId())
                        .fullName(a.getUser().getFirstName() + " " + a.getUser().getLastName())
                        .profileImageUrl(a.getUser().getProfileImageUrl())
                        .build()).toList())
                .build();
    }

    private TicketDetailResponse toDetailResponse(Ticket ticket, List<TicketAssignee> assignees) {
        return TicketDetailResponse.builder()
                .id(ticket.getId())
                .ticketId(formatTicketId(ticket.getTicketYear(), ticket.getTicketSeq()))
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .projectId(ticket.getProject().getId())
                .projectName(ticket.getProject().getName())
                .subCategoryId(ticket.getSubCategory().getId())
                .subCategoryName(ticket.getSubCategory().getName())
                .currentStatusId(ticket.getCurrentStatus().getId())
                .currentStatusName(ticket.getCurrentStatus().getName())
                .currentStatusGroup(ticket.getCurrentStatus().getGroup())
                .statusFlowId(ticket.getStatusFlow().getId())
                .statusFlowName(ticket.getStatusFlow().getName())
                .priorityId(ticket.getSubCategory().getPriorityLevel().getId())
                .priorityName(ticket.getSubCategory().getPriorityLevel().getName())
                .priorityIconShape(ticket.getSubCategory().getPriorityLevel().getIconShape())
                .priorityIconColor(ticket.getSubCategory().getPriorityLevel().getIconColor())
                .priorityIntervalValue(ticket.getSubCategory().getPriorityLevel().getIntervalValue())
                .priorityIntervalUnit(ticket.getSubCategory().getPriorityLevel().getIntervalUnit())
                .dueDate(ticket.getDueDate())
                .requesterId(ticket.getRequester().getId())
                .requesterFullName(ticket.getRequester().getFirstName() + " " + ticket.getRequester().getLastName())
                .requesterProfileImageUrl(ticket.getRequester().getProfileImageUrl())
                .assignees(assignees.stream().map(a -> TicketAssigneeResponse.builder()
                        .id(a.getId())
                        .userId(a.getUser().getId())
                        .fullName(a.getUser().getFirstName() + " " + a.getUser().getLastName())
                        .profileImageUrl(a.getUser().getProfileImageUrl())
                        .assignedAt(a.getCreatedAt())
                        .build()).toList())
                .build();
    }
}
