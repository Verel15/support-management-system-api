package com.ticket.support_management_system_api.features.dashboard.service;

import com.ticket.support_management_system_api.features.dashboard.dto.*;
import com.ticket.support_management_system_api.features.dashboard.repository.DashboardRepository;
import com.ticket.support_management_system_api.features.dashboard.repository.ProjectDashboardRepository;
import com.ticket.support_management_system_api.features.status.enums.EStatusGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final ProjectDashboardRepository projectDashboardRepository;

    public TicketStatusDistributionResponse getTicketStatusDistribution(int year, int month) {
        List<Object[]> rows = dashboardRepository.countTicketsByStatusGroupAndYearMonth(year, month);

        Map<EStatusGroup, Long> groupCounts = rows.stream()
                .collect(Collectors.toMap(
                        r -> (EStatusGroup) r[0],
                        r -> (Long) r[1]
                ));

        long total = groupCounts.values().stream().mapToLong(v -> v).sum();

        List<TicketStatusDistributionResponse.StatusCount> statusCounts = new ArrayList<>();
        for (EStatusGroup group : EStatusGroup.values()) {
            statusCounts.add(TicketStatusDistributionResponse.StatusCount.builder()
                    .group(group.name())
                    .count(groupCounts.getOrDefault(group, 0L))
                    .build());
        }

        return TicketStatusDistributionResponse.builder()
                .total(total)
                .statusCounts(statusCounts)
                .build();
    }

    public TopProjectResponse getTop5ProjectsByTickets(int year, int month) {
        List<Object[]> rows = dashboardRepository.findTop5ProjectsByTicketCountAndYearMonth(year, month);

        List<TopProjectResponse.ProjectTicketCount> projects = rows.stream()
                .limit(5)
                .map(r -> TopProjectResponse.ProjectTicketCount.builder()
                        .projectName((String) r[0])
                        .ticketCount((Long) r[1])
                        .build())
                .toList();

        return TopProjectResponse.builder()
                .projects(projects)
                .build();
    }

    public TicketSummaryResponse getTicketSummary(int year) {
        long open = dashboardRepository.countOpenTicketsByYear(year);
        long overdue = dashboardRepository.countOverdueTicketsByYear(year);
        long success = dashboardRepository.countSuccessTicketsByYear(year);

        long prevOpen = dashboardRepository.countOpenTicketsByYear(year - 1);
        long prevOverdue = dashboardRepository.countOverdueTicketsByYear(year - 1);
        long prevSuccess = dashboardRepository.countSuccessTicketsByYear(year - 1);

        long projectCount = projectDashboardRepository.countDistinctProjectsWithTicketsForYear(year);
        long divisor = projectCount == 0 ? 1 : projectCount;

        long prevProjectCount = projectDashboardRepository.countDistinctProjectsWithTicketsForYear(year - 1);
        long prevDivisor = prevProjectCount == 0 ? 1 : prevProjectCount;

        long avgOpen = open / divisor;
        long avgOverdue = overdue / divisor;
        long avgSuccess = success / divisor;
        long prevAvgOpen = prevOpen / prevDivisor;
        long prevAvgOverdue = prevOverdue / prevDivisor;
        long prevAvgSuccess = prevSuccess / prevDivisor;

        return TicketSummaryResponse.builder()
                .avgOpen(avgOpen)
                .avgOverdue(avgOverdue)
                .avgSuccess(avgSuccess)
                .openTrend(trend(avgOpen, prevAvgOpen))
                .overdueTrend(trend(avgOverdue, prevAvgOverdue))
                .successTrend(trend(avgSuccess, prevAvgSuccess))
                .build();
    }

    private String trend(long current, long previous) {
        if (current > previous) return "up";
        if (current < previous) return "down";
        return "stable";
    }

    public ProjectStatusDistributionResponse getProjectStatusDistribution(int year) {
        LocalDate today = LocalDate.now();
        Long total = projectDashboardRepository.countTotalProjectsForYear(year);
        List<Object[]> rows = projectDashboardRepository.countProjectsByStatusForYear(year, today);
        Object[] row = rows.isEmpty() ? new Object[]{null, null, null} : rows.get(0);

        long waiting = row[0] != null ? ((Number) row[0]).longValue() : 0L;
        long open = row[1] != null ? ((Number) row[1]).longValue() : 0L;
        long close = row[2] != null ? ((Number) row[2]).longValue() : 0L;

        return ProjectStatusDistributionResponse.builder()
                .total(total != null ? total : 0L)
                .waitingCount(waiting)
                .openCount(open)
                .closeCount(close)
                .build();
    }

    public TicketTrendResponse getTicketTrend(int year) {
        List<Object[]> monthly = dashboardRepository.findMonthlyTicketStatsByYear(year);
        List<Object[]> totalRows = dashboardRepository.findYearlyTicketTotals(year);
        Object[] totals = totalRows.isEmpty() ? new Object[]{null, null, null} : totalRows.get(0);

        long totalOpen = totals[0] != null ? ((Number) totals[0]).longValue() : 0L;
        long totalSuccess = totals[1] != null ? ((Number) totals[1]).longValue() : 0L;
        long totalOverdue = totals[2] != null ? ((Number) totals[2]).longValue() : 0L;

        List<TicketTrendResponse.MonthlyData> monthlyData = monthly.stream()
                .map(r -> TicketTrendResponse.MonthlyData.builder()
                        .month(((Number) r[0]).intValue())
                        .openCount(r[1] != null ? ((Number) r[1]).longValue() : 0L)
                        .successCount(r[2] != null ? ((Number) r[2]).longValue() : 0L)
                        .overdueCount(r[3] != null ? ((Number) r[3]).longValue() : 0L)
                        .build())
                .toList();

        return TicketTrendResponse.builder()
                .totalOpen(totalOpen)
                .totalSuccess(totalSuccess)
                .totalOverdue(totalOverdue)
                .monthly(monthlyData)
                .build();
    }
}
