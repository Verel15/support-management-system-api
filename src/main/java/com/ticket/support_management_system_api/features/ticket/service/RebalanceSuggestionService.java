package com.ticket.support_management_system_api.features.ticket.service;

import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.notification.enums.ENotificationType;
import com.ticket.support_management_system_api.features.notification.service.NotificationEventPublisher;
import com.ticket.support_management_system_api.features.project.entities.ProjectMember;
import com.ticket.support_management_system_api.features.project.enums.EProjectMemberRole;
import com.ticket.support_management_system_api.features.project.repository.ProjectMemberRepository;
import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import com.ticket.support_management_system_api.features.ticket.dto.RebalanceSuggestionResponse;
import com.ticket.support_management_system_api.features.ticket.dto.SuggestedAssigneeUser;
import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import com.ticket.support_management_system_api.features.ticket.entities.TicketAssignee;
import com.ticket.support_management_system_api.features.ticket.repository.TicketAssigneeRepository;
import com.ticket.support_management_system_api.features.ticket.repository.TicketRepository;
import com.ticket.support_management_system_api.features.user.entities.ExternalDetails;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.ExternalDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RebalanceSuggestionService {

    private static final List<EStatusGroup> CLOSED_STATUS_GROUPS = List.of(EStatusGroup.SUCCESS, EStatusGroup.FAILED);
    private static final int DUE_SOON_MINUTES = 30;

    private final TicketRepository ticketRepository;
    private final TicketAssigneeRepository ticketAssigneeRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ExternalDetailsRepository externalDetailsRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Transactional(readOnly = true)
    public RebalanceSuggestionResponse getSuggestion(UUID ticketId) {
        Ticket ticket = ticketRepository.findByIdAndArchivedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบ Ticket id: " + ticketId));

        if (CLOSED_STATUS_GROUPS.contains(ticket.getCurrentStatus().getGroup())) {
            return notSuggested("Ticket ปิดแล้ว");
        }
        if (ticket.getDueDate() == null || ticket.getDueDate().isAfter(LocalDateTime.now().plusMinutes(DUE_SOON_MINUTES))) {
            return notSuggested("Ticket ยังไม่ใกล้ครบกำหนด");
        }

        List<TicketAssignee> assignees = ticketAssigneeRepository.findAllByTicketIdAndArchivedAtIsNull(ticketId);
        if (assignees.isEmpty()) {
            return notSuggested("Ticket ยังไม่มีผู้รับผิดชอบ");
        }

        UUID projectId = ticket.getProject().getId();
        UUID positionId = ticket.getSubCategory().getPosition().getId();
        Workload workload = computeWorkload(projectId, positionId);
        if (workload == null) {
            return notSuggested("ไม่มีข้อมูลทีมที่ตำแหน่งเดียวกันในโครงการนี้เพียงพอ");
        }

        for (TicketAssignee assignee : assignees) {
            UUID userId = assignee.getUser().getId();
            long openCount = workload.openCountByUserId.getOrDefault(userId, 0L);
            if (workload.matchingUserIds.contains(userId) && openCount > workload.averageOpenCount) {
                ProjectMember target = workload.leastLoadedOtherThan(userId);
                if (target == null) {
                    continue;
                }
                return RebalanceSuggestionResponse.builder()
                        .suggested(true)
                        .reason(null)
                        .overloadedAssignee(toSuggestedUser(assignee.getUser(), workload, openCount))
                        .suggestedTransferTo(toSuggestedUser(target.getUser(), workload,
                                workload.openCountByUserId.getOrDefault(target.getUser().getId(), 0L)))
                        .build();
            }
        }

        return notSuggested("ผู้รับผิดชอบปัจจุบันยังรับงานไม่เกินค่าเฉลี่ยของทีม");
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void notifyOverloadedAssigneesForDueSoonTickets() {
        List<Ticket> dueSoon = ticketRepository.findDueSoonUnnotified(
                CLOSED_STATUS_GROUPS, LocalDateTime.now().plusMinutes(DUE_SOON_MINUTES));

        for (Ticket ticket : dueSoon) {
            try {
                evaluateAndNotify(ticket);
            } catch (Exception e) {
                log.error("Failed to evaluate rebalance suggestion for ticket {}: {}", ticket.getId(), e.getMessage());
            }
        }
    }

    private void evaluateAndNotify(Ticket ticket) {
        RebalanceSuggestionResponse suggestion = getSuggestion(ticket.getId());
        ticket.setRebalanceSuggestedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        if (!suggestion.isSuggested()) {
            return;
        }

        SuggestedAssigneeUser overloaded = suggestion.getOverloadedAssignee();
        SuggestedAssigneeUser target = suggestion.getSuggestedTransferTo();
        notificationEventPublisher.publishDirectEvent(
                ENotificationType.TICKET_REBALANCE_SUGGESTED, overloaded.getUserId(), ticket.getId(),
                "Ticket ใกล้ครบกำหนด: " + ticket.getTitle(),
                "คุณมี Ticket ค้างอยู่มาก แนะนำให้โอนงานนี้ให้ " + target.getFullName() + " ซึ่งมีงานค้างน้อยกว่า",
                Map.of(
                        "ticketTitle", ticket.getTitle(),
                        "suggestedTransferToUserId", target.getUserId(),
                        "suggestedTransferToName", target.getFullName()
                ));
    }

    private RebalanceSuggestionResponse notSuggested(String reason) {
        return RebalanceSuggestionResponse.builder().suggested(false).reason(reason).build();
    }

    private SuggestedAssigneeUser toSuggestedUser(User user, Workload workload, long openCount) {
        ExternalDetails details = workload.detailsByUserId.get(user.getId());
        return SuggestedAssigneeUser.builder()
                .userId(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .profileImageUrl(user.getProfileImageUrl())
                .positionName(details != null && details.getPosition() != null ? details.getPosition().getName() : null)
                .openTicketCount(openCount)
                .build();
    }

    private Workload computeWorkload(UUID projectId, UUID positionId) {
        List<ProjectMember> assigneeMembers = projectMemberRepository
                .findAllByProjectIdAndRoleAndArchivedAtIsNull(projectId, EProjectMemberRole.ASSIGNEE);
        if (assigneeMembers.isEmpty()) {
            return null;
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
        if (matchingMembers.size() < 2) {
            return null;
        }

        List<UUID> matchingUserIds = matchingMembers.stream().map(m -> m.getUser().getId()).toList();
        Map<UUID, Long> openCountByUserId = ticketAssigneeRepository
                .countOpenTicketsByUserIds(matchingUserIds, CLOSED_STATUS_GROUPS)
                .stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));

        double averageOpenCount = matchingUserIds.stream()
                .mapToLong(id -> openCountByUserId.getOrDefault(id, 0L))
                .average()
                .orElse(0);

        return new Workload(matchingMembers, matchingUserIds, openCountByUserId, detailsByUserId, averageOpenCount);
    }

    private record Workload(List<ProjectMember> matchingMembers, List<UUID> matchingUserIds,
                             Map<UUID, Long> openCountByUserId, Map<UUID, ExternalDetails> detailsByUserId,
                             double averageOpenCount) {

        ProjectMember leastLoadedOtherThan(UUID excludedUserId) {
            return matchingMembers.stream()
                    .filter(m -> !m.getUser().getId().equals(excludedUserId))
                    .min(Comparator.comparingLong(m -> openCountByUserId.getOrDefault(m.getUser().getId(), 0L)))
                    .orElse(null);
        }
    }
}
