package com.ticket.support_management_system_api.features.ticket_type.repository;

import com.ticket.support_management_system_api.common.utils.DateRangeUtils;
import com.ticket.support_management_system_api.features.ticket_type.dto.TicketTypeFilterRequest;
import com.ticket.support_management_system_api.features.ticket_type.entities.TicketType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketTypeSpecification {

    public static Specification<TicketType> active(TicketTypeFilterRequest filter) {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
