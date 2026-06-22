package com.ticket.support_management_system_api.features.user_type;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.support_management_system_api.features.user_type.entities.QUserType;
import com.ticket.support_management_system_api.features.user_type.entities.UserType;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UserTypeRepositoryImpl implements UserTypeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QUserType q = QUserType.userType;

    @Override
    public Optional<UserType> findByName(String name) {
        return Optional.ofNullable(
                queryFactory.selectFrom(q)
                        .where(q.name.eq(name))
                        .fetchOne()
        );
    }

    @Override
    public List<UserType> findAllOrderByNameAsc() {
        return queryFactory.selectFrom(q)
                .orderBy(q.name.asc())
                .fetch();
    }

    @Override
    public List<UserType> findAllActiveOrderByNameAsc() {
        return queryFactory.selectFrom(q)
                .where(q.archivedAt.isNull())
                .orderBy(q.name.asc())
                .fetch();
    }

    @Override
    public boolean existsByName(String name) {
        return queryFactory.selectFrom(q)
                .where(q.name.eq(name))
                .fetchFirst() != null;
    }

    @Override
    public boolean existsByNameAndIdNot(String name, UUID id) {
        return queryFactory.selectFrom(q)
                .where(q.name.eq(name).and(q.id.ne(id)))
                .fetchFirst() != null;
    }
}
