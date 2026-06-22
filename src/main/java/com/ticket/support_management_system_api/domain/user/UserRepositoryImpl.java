package com.ticket.support_management_system_api.domain.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.support_management_system_api.domain.user.dto.UserFilterRequest;
import com.ticket.support_management_system_api.domain.user.entities.User;
import com.ticket.support_management_system_api.domain.user.entities.QUser;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QUser q = QUser.user;

    @Override
    public Page<User> findAllActive(UserFilterRequest filter, Pageable pageable) {
        BooleanBuilder predicate = new BooleanBuilder();
        predicate.and(q.archivedAt.isNull());

        if (filter.getAccountType() != null) {
            predicate.and(q.accountType.eq(filter.getAccountType()));
        }

        if (filter.getCreatedWithinDays() != null) {
            predicate.and(q.createdAt.goe(LocalDateTime.now().minusDays(filter.getCreatedWithinDays())));
        }

        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            String kw = filter.getKeyword().trim();
            predicate.and(
                q.firstName.containsIgnoreCase(kw)
                    .or(q.lastName.containsIgnoreCase(kw))
                    .or(q.firstName.concat(" ").concat(q.lastName).containsIgnoreCase(kw))
                    .or(q.email.containsIgnoreCase(kw))
            );
        }

        Long total = queryFactory
                .select(q.id.count())
                .from(q)
                .where(predicate)
                .fetchOne();

        List<User> content = queryFactory.selectFrom(q)
                .where(predicate)
                .orderBy(q.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
