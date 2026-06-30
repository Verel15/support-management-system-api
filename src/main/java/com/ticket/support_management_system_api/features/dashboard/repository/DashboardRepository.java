package com.ticket.support_management_system_api.features.dashboard.repository;

import com.ticket.support_management_system_api.features.ticket.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardRepository extends JpaRepository<Ticket, UUID> {

    // Ticket count grouped by status group, filtered by year+month
    @Query("""
            SELECT s.group, COUNT(t)
            FROM Ticket t
            JOIN t.currentStatus s
            WHERE t.archivedAt IS NULL
              AND YEAR(t.createdAt) = :year
              AND MONTH(t.createdAt) = :month
            GROUP BY s.group
            """)
    List<Object[]> countTicketsByStatusGroupAndYearMonth(@Param("year") int year, @Param("month") int month);

    // Top 5 projects by ticket count, filtered by year+month
    @Query("""
            SELECT p.name, COUNT(t)
            FROM Ticket t
            JOIN t.project p
            WHERE t.archivedAt IS NULL
              AND p.archivedAt IS NULL
              AND YEAR(t.createdAt) = :year
              AND MONTH(t.createdAt) = :month
            GROUP BY p.id, p.name
            ORDER BY COUNT(t) DESC
            """)
    List<Object[]> findTop5ProjectsByTicketCountAndYearMonth(@Param("year") int year, @Param("month") int month);

    // Avg open tickets per month for a year (tickets created in that year, START group)
    @Query("""
            SELECT COUNT(t)
            FROM Ticket t
            JOIN t.currentStatus s
            WHERE t.archivedAt IS NULL
              AND YEAR(t.createdAt) = :year
              AND s.group = com.ticket.support_management_system_api.features.status.enums.StatusGroup.START
            """)
    Long countOpenTicketsByYear(@Param("year") int year);

    // Count tickets overdue in a year (dueDate < now, not SUCCESS/FAILED)
    @Query("""
            SELECT COUNT(t)
            FROM Ticket t
            JOIN t.currentStatus s
            WHERE t.archivedAt IS NULL
              AND YEAR(t.createdAt) = :year
              AND t.dueDate IS NOT NULL
              AND t.dueDate < CURRENT_TIMESTAMP
              AND s.group NOT IN (
                  com.ticket.support_management_system_api.features.status.enums.StatusGroup.SUCCESS,
                  com.ticket.support_management_system_api.features.status.enums.StatusGroup.FAILED
              )
            """)
    Long countOverdueTicketsByYear(@Param("year") int year);

    // Count tickets closed successfully in a year (SUCCESS group)
    @Query("""
            SELECT COUNT(t)
            FROM Ticket t
            JOIN t.currentStatus s
            WHERE t.archivedAt IS NULL
              AND YEAR(t.createdAt) = :year
              AND s.group = com.ticket.support_management_system_api.features.status.enums.StatusGroup.SUCCESS
            """)
    Long countSuccessTicketsByYear(@Param("year") int year);

    // Monthly open/success/overdue ticket counts for line chart
    @Query("""
            SELECT MONTH(t.createdAt),
                   SUM(CASE WHEN s.group = com.ticket.support_management_system_api.features.status.enums.StatusGroup.START THEN 1 ELSE 0 END),
                   SUM(CASE WHEN s.group = com.ticket.support_management_system_api.features.status.enums.StatusGroup.SUCCESS THEN 1 ELSE 0 END),
                   SUM(CASE WHEN (t.dueDate IS NOT NULL AND t.dueDate < CURRENT_TIMESTAMP
                       AND s.group NOT IN (
                           com.ticket.support_management_system_api.features.status.enums.StatusGroup.SUCCESS,
                           com.ticket.support_management_system_api.features.status.enums.StatusGroup.FAILED
                       )) THEN 1 ELSE 0 END)
            FROM Ticket t
            JOIN t.currentStatus s
            WHERE t.archivedAt IS NULL
              AND YEAR(t.createdAt) = :year
            GROUP BY MONTH(t.createdAt)
            ORDER BY MONTH(t.createdAt)
            """)
    List<Object[]> findMonthlyTicketStatsByYear(@Param("year") int year);

    // Total open/success/overdue for the year (for legend totals in line chart)
    @Query("""
            SELECT
                SUM(CASE WHEN s.group = com.ticket.support_management_system_api.features.status.enums.StatusGroup.START THEN 1 ELSE 0 END),
                SUM(CASE WHEN s.group = com.ticket.support_management_system_api.features.status.enums.StatusGroup.SUCCESS THEN 1 ELSE 0 END),
                SUM(CASE WHEN (t.dueDate IS NOT NULL AND t.dueDate < CURRENT_TIMESTAMP
                    AND s.group NOT IN (
                        com.ticket.support_management_system_api.features.status.enums.StatusGroup.SUCCESS,
                        com.ticket.support_management_system_api.features.status.enums.StatusGroup.FAILED
                    )) THEN 1 ELSE 0 END)
            FROM Ticket t
            JOIN t.currentStatus s
            WHERE t.archivedAt IS NULL
              AND YEAR(t.createdAt) = :year
            """)
    List<Object[]> findYearlyTicketTotals(@Param("year") int year);
}
