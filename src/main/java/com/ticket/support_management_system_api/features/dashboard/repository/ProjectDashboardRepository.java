package com.ticket.support_management_system_api.features.dashboard.repository;

import com.ticket.support_management_system_api.features.project.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ProjectDashboardRepository extends JpaRepository<Project, UUID> {

    // Count projects by status (waiting/open/close) for a given year
    // Status derived from startDate/endDate vs :today
    @Query("""
            SELECT
                SUM(CASE WHEN p.startDate > :today THEN 1 ELSE 0 END),
                SUM(CASE WHEN p.startDate <= :today AND p.endDate >= :today THEN 1 ELSE 0 END),
                SUM(CASE WHEN p.endDate < :today THEN 1 ELSE 0 END)
            FROM Project p
            WHERE p.archivedAt IS NULL
              AND YEAR(p.startDate) <= :year
              AND YEAR(p.endDate) >= :year
            """)
    List<Object[]> countProjectsByStatusForYear(@Param("year") int year, @Param("today") LocalDate today);

    // Total project count for a year
    @Query("""
            SELECT COUNT(p)
            FROM Project p
            WHERE p.archivedAt IS NULL
              AND YEAR(p.startDate) <= :year
              AND YEAR(p.endDate) >= :year
            """)
    Long countTotalProjectsForYear(@Param("year") int year);

    // Avg tickets per project for a year (for ticket summary card)
    @Query("""
            SELECT COUNT(t)
            FROM Ticket t
            WHERE t.archivedAt IS NULL
              AND YEAR(t.createdAt) = :year
            """)
    Long countTotalTicketsForYear(@Param("year") int year);

    @Query("""
            SELECT COUNT(DISTINCT t.project.id)
            FROM Ticket t
            WHERE t.archivedAt IS NULL
              AND YEAR(t.createdAt) = :year
            """)
    Long countDistinctProjectsWithTicketsForYear(@Param("year") int year);
}
