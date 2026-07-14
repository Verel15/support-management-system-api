package com.ticket.support_management_system_api.features.project.repository;

import com.ticket.support_management_system_api.common.utils.DateRangeUtils;
import com.ticket.support_management_system_api.features.project.dto.ProjectFilterRequest;
import com.ticket.support_management_system_api.features.project.entities.Project;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectSpecification {

    public static Specification<Project> active(ProjectFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("archivedAt")));

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), kw));
            }

            LocalDateTime start = DateRangeUtils.resolveStart(filter.getDateRange());
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }

            if (filter.getStatus() != null) {
                LocalDate today = LocalDate.now();
                predicates.add(switch (filter.getStatus()) {
                    case WAITING -> cb.greaterThan(root.get("startDate"), today);
                    case OPEN -> cb.and(
                            cb.lessThanOrEqualTo(root.get("startDate"), today),
                            cb.greaterThanOrEqualTo(root.get("endDate"), today));
                    case CLOSED -> cb.lessThan(root.get("endDate"), today);
                });
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Project> visibleToCustomer(UUID companyId, List<UUID> memberProjectIds) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("company").get("id"), companyId),
                root.get("id").in(memberProjectIds)
        );
    }
}
