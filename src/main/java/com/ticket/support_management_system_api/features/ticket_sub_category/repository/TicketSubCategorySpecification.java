package com.ticket.support_management_system_api.features.ticket_sub_category.repository;

import com.ticket.support_management_system_api.common.utils.DateRangeUtils;
import com.ticket.support_management_system_api.features.ticket_sub_category.dto.TicketSubCategoryFilterRequest;
import com.ticket.support_management_system_api.features.ticket_sub_category.entities.TicketSubCategory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketSubCategorySpecification {

    public static Specification<TicketSubCategory> active(TicketSubCategoryFilterRequest filter) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("priorityLevel", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("position", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("category", jakarta.persistence.criteria.JoinType.LEFT);
            }

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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
