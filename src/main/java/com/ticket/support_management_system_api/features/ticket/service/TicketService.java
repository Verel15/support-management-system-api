package com.ticket.support_management_system_api.features.ticket.service;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.exception.BadRequestException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.notification.enums.ENotificationType;
import com.ticket.support_management_system_api.features.notification.service.NotificationEventPublisher;
import com.ticket.support_management_system_api.common.response.PageResponse;
import com.ticket.support_management_system_api.common.utils.PaginationUtils;
import com.ticket.support_management_system_api.common.utils.RemainingTimeUtils;
import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;
import com.ticket.support_management_system_api.features.priority.enums.EIntervalUnit;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.entities.ProjectMember;
import com.ticket.support_management_system_api.features.project.enums.EProjectMemberRole;
import com.ticket.support_management_system_api.features.project.repository.ProjectMemberRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.status.entities.StatusFlows;
import com.ticket.support_management_system_api.features.status.entities.Statuses;
import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import com.ticket.support_management_system_api.features.status.repository.StatusRepository;
import com.ticket.support_management_system_api.features.ticket.dto.*;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.entities.TicketAssignee;
import com.ticket.support_management_system_api.features.ticket.entities.TicketAssigneeLog;
import com.ticket.support_management_system_api.features.ticket.entities.TicketStatusLog;
import com.ticket.support_management_system_api.features.ticket.entities.TicketUpdateLog;
import com.ticket.support_management_system_api.features.ticket.entities.TicketYearCounter;
import com.ticket.support_management_system_api.features.ticket.enums.ESuggestedAssigneeReason;
import com.ticket.support_management_system_api.features.ticket.enums.ETicketAssigneeAction;
import com.ticket.support_management_system_api.features.ticket.repository.TicketAssigneeLogRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketAssigneeRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketStatusLogRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketUpdateLogRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketYearCounterRepository;
import com.ticket.support_management_system_api.features.ticket_sub_category.entities.TicketSubCategory;
import com.ticket.support_management_system_api.features.ticket_sub_category.repository.TicketSubCategoryRepository;
import com.ticket.support_management_system_api.features.user.entities.ExternalDetails;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.ExternalDetailsRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final NotificationEventPublisher notificationEventPublisher;
    private final ProjectMemberRepository projectMemberRepository;
    private final ExternalDetailsRepository externalDetailsRepository;
    private final TicketAssigneeLogRepository ticketAssigneeLogRepository;
    private final TicketUpdateLogRepository ticketUpdateLogRepository;

    private static final List<EStatusGroup> CLOSED_STATUS_GROUPS = List.of(EStatusGroup.SUCCESS, EStatusGroup.FAILED);

    @Transactional(readOnly = true)
    public PageResponse<TicketListResponse> findAll(TicketFilterRequest filter, Pageable pageable) {
        return queryTickets(buildSpec(filter), pageable);
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse findById(UUID id) {
        Ticket ticket = getOrThrow(id);
        List<TicketAssignee> assignees = ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(id);
        return toDetailResponse(ticket, assignees);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketListResponse> findMy(TicketFilterRequest filter, Pageable pageable, JwtPrincipal user) {
        return queryTickets(buildMySpec(filter, user), pageable);
    }

    @Transactional(readOnly = true)
    public TicketDetailResponse findMyById(UUID id, JwtPrincipal user) {
        Ticket ticket = getOrThrow(id);
        if (user.accountType() == AccountType.CUSTOMER && !ticket.getRequester().getId().equals(user.userId())) {
            throw new ResourceNotFoundException("ไม่พบ Ticket id: " + id);
        }
        List<TicketAssignee> assignees = ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(id);
        return toDetailResponse(ticket, assignees);
    }

    @Transactional(readOnly = true)
    public SuggestedAssigneeResponse getSuggestedAssignee(UUID ticketId) {
        Ticket ticket = getOrThrow(ticketId);
        BestAssigneeResult result = findBestAssignee(ticket.getProject().getId(), ticket.getSubCategory().getPosition().getId());
        if (result.reason() != null) {
            return SuggestedAssigneeResponse.builder().suggested(null).reason(result.reason()).build();
        }

        User bestUser = result.member().getUser();
        ExternalDetails bestDetails = result.detailsByUserId().get(bestUser.getId());
        SuggestedAssigneeUser suggested = SuggestedAssigneeUser.builder()
                .userId(bestUser.getId())
                .fullName(bestUser.getFirstName() + " " + bestUser.getLastName())
                .profileImageUrl(bestUser.getProfileImageUrl())
                .positionName(bestDetails.getPosition().getName())
                .openTicketCount(result.openTicketCount())
                .build();

        return SuggestedAssigneeResponse.builder()
                .suggested(suggested)
                .reason(null)
                .build();
    }

    private record BestAssigneeResult(ProjectMember member, long openTicketCount,
                                       Map<UUID, ExternalDetails> detailsByUserId, ESuggestedAssigneeReason reason) {
        static BestAssigneeResult empty(ESuggestedAssigneeReason reason) {
            return new BestAssigneeResult(null, 0, Map.of(), reason);
        }
    }

    private BestAssigneeResult findBestAssignee(UUID projectId, UUID positionId) {
        List<ProjectMember> assigneeMembers = projectMemberRepository
                .findAllByProjectIdAndRoleAndArchivedAtIsNull(projectId, EProjectMemberRole.ASSIGNEE);
        if (assigneeMembers.isEmpty()) {
            return BestAssigneeResult.empty(ESuggestedAssigneeReason.NO_PROJECT_MEMBERS);
        }

        List<UUID> candidateUserIds = assigneeMembers.stream().map(m -> m.getUser().getId()).toList();
        Map<UUID, ExternalDetails> detailsByUserId = externalDetailsRepository.findAllByUserIdIn(candidateUserIds)
                .stream()
                .collect(Collectors.toMap(d -> d.getUser().getId(), d -> d));

        List<ProjectMember> matchingMembers = assigneeMembers.stream()
                .filter(m -> {
                    ExternalDetails details = detailsByUserId.get(m.getUser().getId());
                    return details != null && details.getPosition() != null
                            && details.getPosition().getId().equals(positionId);
                })
                .toList();
        if (matchingMembers.isEmpty()) {
            return BestAssigneeResult.empty(ESuggestedAssigneeReason.NO_POSITION_MATCH);
        }

        List<UUID> matchingUserIds = matchingMembers.stream().map(m -> m.getUser().getId()).toList();
        Map<UUID, Long> openCountByUserId = ticketAssigneeRepository
                .countOpenTicketsByUserIds(matchingUserIds, CLOSED_STATUS_GROUPS)
                .stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));

        ProjectMember best = matchingMembers.stream()
                .min(Comparator.comparingLong(m -> openCountByUserId.getOrDefault(m.getUser().getId(), 0L)))
                .orElseThrow();

        return new BestAssigneeResult(best, openCountByUserId.getOrDefault(best.getUser().getId(), 0L), detailsByUserId, null);
    }

    private PageResponse<TicketListResponse> queryTickets(Specification<Ticket> spec, Pageable pageable) {
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
                .ticketNo(formatTicketId(year, seq))
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

        notificationEventPublisher.publishTicketEvent(
                ENotificationType.TICKET_CREATED, ticket.getId(), actorUserId,
                "Ticket ใหม่: " + ticket.getTitle(),
                actor.getFirstName() + " " + actor.getLastName() + " สร้าง Ticket ใหม่",
                Map.of("ticketTitle", ticket.getTitle()));

        List<TicketAssignee> assignees = autoAssign(ticket, actor);

        return toDetailResponse(ticket, assignees);
    }

    private List<TicketAssignee> autoAssign(Ticket ticket, User actor) {
        BestAssigneeResult result = findBestAssignee(ticket.getProject().getId(), ticket.getSubCategory().getPosition().getId());
        if (result.reason() != null) {
            return List.of();
        }

        User assigneeUser = result.member().getUser();
        TicketAssignee assignee = ticketAssigneeRepository.save(
                TicketAssignee.builder().ticket(ticket).user(assigneeUser).build());

        ticketAssigneeLogRepository.save(TicketAssigneeLog.builder()
                .ticket(ticket)
                .changedBy(actor)
                .assigneeUser(assigneeUser)
                .action(ETicketAssigneeAction.ADDED)
                .build());

        notificationEventPublisher.publishTicketEvent(
                ENotificationType.TICKET_ASSIGNED, ticket.getId(), null,
                "มอบหมาย Ticket ให้ " + assigneeUser.getFirstName() + " " + assigneeUser.getLastName(),
                assigneeUser.getFirstName() + " " + assigneeUser.getLastName() + " ถูกมอบหมายให้รับผิดชอบ Ticket โดยระบบอัตโนมัติ",
                Map.of("assigneeName", assigneeUser.getFirstName() + " " + assigneeUser.getLastName()));

        return List.of(assignee);
    }

    public TicketDetailResponse update(UUID id, UpdateTicketRequest request, UUID actorUserId) {
        Ticket ticket = getOrThrow(id);

        Project project = projectRepository.findByIdAndArchivedAtIsNull(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบโครงการ id: " + request.getProjectId()));
        TicketSubCategory subCategory = subCategoryRepository.findByIdAndArchivedAtIsNull(request.getSubCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบประเภทย่อย Ticket id: " + request.getSubCategoryId()));
        StatusFlows statusFlow = subCategory.getCategory().getStatusFlow();
        boolean statusFlowChanged = !ticket.getStatusFlow().getId().equals(statusFlow.getId());
        boolean projectChanged = !ticket.getProject().getId().equals(project.getId());

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + actorUserId));

        String oldTitle = ticket.getTitle();
        String oldDescription = ticket.getDescription();
        String oldProjectName = ticket.getProject().getName();
        String oldSubCategoryName = ticket.getSubCategory().getName();
        LocalDateTime oldDueDate = ticket.getDueDate();

        List<TicketUpdateLog> updateLogs = new ArrayList<>();

        if (request.getTitle() != null && !request.getTitle().equals(oldTitle)) {
            ticket.setTitle(request.getTitle());
            updateLogs.add(buildUpdateLog(ticket, actor, "title", oldTitle, request.getTitle()));
        }
        if (request.getDescription() != null && !request.getDescription().equals(oldDescription)) {
            ticket.setDescription(request.getDescription());
            updateLogs.add(buildUpdateLog(ticket, actor, "description", oldDescription, request.getDescription()));
        }
        if (projectChanged) {
            updateLogs.add(buildUpdateLog(ticket, actor, "project", oldProjectName, project.getName()));
        }
        if (!ticket.getSubCategory().getId().equals(subCategory.getId())) {
            updateLogs.add(buildUpdateLog(ticket, actor, "subCategory", oldSubCategoryName, subCategory.getName()));
        }
        ticket.setProject(project);
        ticket.setSubCategory(subCategory);
        ticket.setStatusFlow(statusFlow);
        ticket.setDueDate(computeDueDate(ticket.getCreatedAt(), subCategory.getPriorityLevel()));
        if (!ticket.getDueDate().equals(oldDueDate)) {
            updateLogs.add(buildUpdateLog(ticket, actor, "dueDate", String.valueOf(oldDueDate), String.valueOf(ticket.getDueDate())));
        }

        if (statusFlowChanged) {
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
        ticketUpdateLogRepository.saveAll(updateLogs);

        if (projectChanged) {
            reassignForProjectChange(ticket, actor);
        }

        notificationEventPublisher.publishTicketEvent(
                ENotificationType.TICKET_UPDATED, ticket.getId(), actorUserId,
                "อัพเดต Ticket: " + ticket.getTitle(),
                "มีการอัพเดตข้อมูล Ticket",
                Map.of("ticketTitle", ticket.getTitle()));

        List<TicketAssignee> assignees = ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(id);
        return toDetailResponse(ticket, assignees);
    }

    private TicketUpdateLog buildUpdateLog(Ticket ticket, User actor, String fieldName, String oldValue, String newValue) {
        return TicketUpdateLog.builder()
                .ticket(ticket)
                .changedBy(actor)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
    }

    private void reassignForProjectChange(Ticket ticket, User actor) {
        List<TicketAssignee> currentAssignees = ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(ticket.getId());
        boolean anyRemoved = false;
        for (TicketAssignee assignee : currentAssignees) {
            boolean stillMember = projectMemberRepository.existsByProjectIdAndUserIdAndArchivedAtIsNull(
                    ticket.getProject().getId(), assignee.getUser().getId());
            if (stillMember) {
                continue;
            }
            anyRemoved = true;
            assignee.setArchivedAt(LocalDateTime.now());
            assignee.setArchivedBy(actor.getId());
            ticketAssigneeRepository.save(assignee);

            ticketAssigneeLogRepository.save(TicketAssigneeLog.builder()
                    .ticket(ticket)
                    .changedBy(actor)
                    .assigneeUser(assignee.getUser())
                    .action(ETicketAssigneeAction.REMOVED)
                    .build());

            notificationEventPublisher.publishTicketEvent(
                    ENotificationType.TICKET_UNASSIGNED, ticket.getId(), actor.getId(),
                    "ยกเลิกการมอบหมาย Ticket",
                    assignee.getUser().getFirstName() + " " + assignee.getUser().getLastName() + " ถูกยกเลิกการมอบหมายเนื่องจากเปลี่ยนโครงการ",
                    Map.of());
        }

        if (anyRemoved) {
            ticket.setRebalanceSuggestedAt(null);
            ticketRepository.save(ticket);
            autoAssign(ticket, actor);
        }
    }

    public void delete(UUID id, UUID actorUserId) {
        Ticket ticket = getOrThrow(id);
        ticket.setArchivedAt(LocalDateTime.now());
        ticket.setArchivedBy(actorUserId);
        ticketRepository.save(ticket);
        notificationEventPublisher.publishTicketEvent(
                ENotificationType.TICKET_DELETED, id, actorUserId,
                "ลบ Ticket: " + ticket.getTitle(),
                "Ticket ถูกลบออกจากระบบ",
                Map.of("ticketTitle", ticket.getTitle()));
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

        notificationEventPublisher.publishTicketEvent(
                ENotificationType.TICKET_STATUS_CHANGED, ticket.getId(), actorUserId,
                "สถานะ Ticket เปลี่ยน: " + ticket.getTitle(),
                "สถานะเปลี่ยนจาก " + fromStatus.getName() + " เป็น " + toStatus.getName(),
                Map.of(
                        "ticketTitle", ticket.getTitle(),
                        "fromStatusName", fromStatus.getName(),
                        "fromStatusGroup", fromStatus.getGroup().name(),
                        "toStatusName", toStatus.getName(),
                        "toStatusGroup", toStatus.getGroup().name()
                ));

        List<TicketAssignee> assignees = ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(id);
        return toDetailResponse(ticket, assignees);
    }

    public TicketDetailResponse updateDueDate(UUID id, UpdateTicketDueDateRequest request, UUID actorUserId) {
        Ticket ticket = getOrThrow(id);
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบผู้ใช้ id: " + actorUserId));

        LocalDateTime oldDueDate = ticket.getDueDate();
        ticket.setDueDate(request.getDueDate());
        ticket = ticketRepository.save(ticket);
        ticketUpdateLogRepository.save(buildUpdateLog(ticket, actor, "dueDate", String.valueOf(oldDueDate), String.valueOf(ticket.getDueDate())));

        notificationEventPublisher.publishTicketEvent(
                ENotificationType.TICKET_UPDATED, ticket.getId(), actorUserId,
                "อัพเดตครบกำหนด Ticket: " + ticket.getTitle(),
                "มีการเปลี่ยนวันครบกำหนดของ Ticket",
                Map.of(
                        "ticketTitle", ticket.getTitle(),
                        "oldDueDate", String.valueOf(oldDueDate),
                        "newDueDate", String.valueOf(ticket.getDueDate())
                ));

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

    private String formatTicketId(int year, int seq) {
        return String.format("TK-%d-%04d", year, seq);
    }

    private LocalDateTime toLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
    }

    private Ticket getOrThrow(UUID id) {
        return ticketRepository.findByIdAndArchivedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบ Ticket id: " + id));
    }

    private Statuses resolveStartStatus(UUID statusFlowId) {
        return statusRepository.findFirstByFlowIdAndGroupOrderBySequenceAsc(statusFlowId, EStatusGroup.START)
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

    private Specification<Ticket> buildMySpec(TicketFilterRequest filter, JwtPrincipal user) {
        return buildSpec(filter).and((root, query, cb) ->
                user.accountType() == AccountType.CUSTOMER
                        ? cb.equal(root.get("requester").get("id"), user.userId())
                        : cb.conjunction());
    }

    private Specification<Ticket> buildSpec(TicketFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("archivedAt")));

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                Expression<Integer> successLast = cb.<Integer>selectCase()
                        .when(cb.equal(root.get("currentStatus").get("group"), EStatusGroup.SUCCESS), 1)
                        .otherwise(0);
                query.orderBy(cb.asc(successLast), cb.asc(root.get("dueDate")));
            }

            if (filter.getProjectId() != null) {
                predicates.add(cb.equal(root.get("project").get("id"), filter.getProjectId()));
            }
            if (filter.getStatusId() != null) {
                predicates.add(cb.equal(root.get("currentStatus").get("id"), filter.getStatusId()));
            }
            if (filter.getStatusGroup() != null) {
                predicates.add(cb.equal(root.get("currentStatus").get("group"), filter.getStatusGroup()));
            }
            if (filter.getPriorityId() != null) {
                predicates.add(cb.equal(root.get("subCategory").get("priorityLevel").get("id"), filter.getPriorityId()));
            }
            if (filter.getStatusFlowId() != null) {
                predicates.add(cb.equal(root.get("statusFlow").get("id"), filter.getStatusFlowId()));
            }
            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), kw));
            }
            if (filter.getDateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), toLocalDateTime(filter.getDateFrom())));
            }
            if (filter.getDateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toLocalDateTime(filter.getDateTo())));
            }
            if (Boolean.TRUE.equals(filter.getOverdue())) {
                predicates.add(cb.lessThan(root.get("dueDate"), LocalDateTime.now()));
            }
            if (filter.getRemainingTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                predicates.add(switch (filter.getRemainingTime()) {
                    case LESS_THAN_30_MIN -> cb.and(
                            cb.greaterThanOrEqualTo(root.get("dueDate"), now),
                            cb.lessThan(root.get("dueDate"), now.plusMinutes(30)));
                    case LESS_THAN_1_DAY -> cb.and(
                            cb.greaterThanOrEqualTo(root.get("dueDate"), now),
                            cb.lessThan(root.get("dueDate"), now.plusDays(1)));
                    case LESS_THAN_3_DAYS -> cb.and(
                            cb.greaterThanOrEqualTo(root.get("dueDate"), now),
                            cb.lessThan(root.get("dueDate"), now.plusDays(3)));
                    case LESS_THAN_7_DAYS -> cb.and(
                            cb.greaterThanOrEqualTo(root.get("dueDate"), now),
                            cb.lessThan(root.get("dueDate"), now.plusDays(7)));
                    case OVERDUE -> cb.lessThan(root.get("dueDate"), now);
                });
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String resolveRemainingTime(Ticket ticket) {
        if (ticket.getCurrentStatus().getGroup() == EStatusGroup.SUCCESS) {
            return RemainingTimeUtils.resolveClosed(ticket.getDueDate(), ticket.getUpdatedAt());
        }
        return RemainingTimeUtils.resolve(ticket.getDueDate());
    }

    private TicketListResponse toListResponse(Ticket ticket, List<TicketAssignee> assignees) {
        return TicketListResponse.builder()
                .id(ticket.getId())
                .ticketId(ticket.getTicketNo())
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
                .remainingTime(resolveRemainingTime(ticket))
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
                .ticketId(ticket.getTicketNo())
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
                .remainingTime(resolveRemainingTime(ticket))
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
