package com.ticket.support_management_system_api.features.user.repository;

import com.ticket.support_management_system_api.common.utils.DateRangeUtils;
import com.ticket.support_management_system_api.features.user.dto.UserFilterRequest;
import com.ticket.support_management_system_api.features.user.entities.CustomerDetails;
import com.ticket.support_management_system_api.features.user.entities.User;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSpecification {

    public static Specification<User> active(UserFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("archivedAt")));

            if (filter.getAccountType() != null) {
                predicates.add(cb.equal(root.get("accountType"), filter.getAccountType()));
            }

            LocalDateTime start = DateRangeUtils.resolveStart(filter.getDateRange());
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String kw = "%" + filter.getKeyword().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), kw),
                        cb.like(cb.lower(root.get("lastName")), kw),
                        cb.like(cb.lower(root.get("email")), kw),
                        cb.like(cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))), kw)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> byCompanyAndKeyword(UUID companyId, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("archivedAt")));

            Subquery<UUID> sub = query.subquery(UUID.class);
            var cdRoot = sub.from(CustomerDetails.class);
            sub.select(cdRoot.get("userId")).where(cb.equal(cdRoot.get("company").get("id"), companyId));
            predicates.add(root.get("id").in(sub));

            if (keyword != null && !keyword.isBlank()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), kw),
                        cb.like(cb.lower(root.get("lastName")), kw),
                        cb.like(cb.lower(root.get("email")), kw),
                        cb.like(cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))), kw)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
