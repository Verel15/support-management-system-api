package com.ticket.support_management_system_api.features.priority.repository;

import com.ticket.support_management_system_api.features.priority.dto.PriorityFilterRequest;
import com.ticket.support_management_system_api.features.priority.entities.PriorityLevels;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class PrioritySpecification {

    public static Specification<PriorityLevels> active(PriorityFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("archivedAt")));

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), kw));
            }

            if (filter.getDateRange() != null) {
                LocalDateTime start;
                switch (filter.getDateRange()) {
                    case TODAY -> start = LocalDate.now().atStartOfDay();
                    case THIS_WEEK -> start = LocalDate.now()
                            .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                            .atStartOfDay();
                    case THIS_MONTH -> start = LocalDate.now()
                            .with(TemporalAdjusters.firstDayOfMonth())
                            .atStartOfDay();
                    default -> start = null;
                }
                if (start != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
