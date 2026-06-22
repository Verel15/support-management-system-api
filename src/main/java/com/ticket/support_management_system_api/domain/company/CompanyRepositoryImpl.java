package com.ticket.support_management_system_api.domain.company;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticket.support_management_system_api.domain.company.entities.Company;
import com.ticket.support_management_system_api.domain.company.entities.QCompany;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QCompany q = QCompany.company;

    @Override
    public Optional<Company> findByName(String name) {
        return Optional.ofNullable(
                queryFactory.selectFrom(q)
                        .where(q.name.eq(name))
                        .fetchOne()
        );
    }

    @Override
    public List<Company> findAllOrderByNameAsc() {
        return queryFactory.selectFrom(q)
                .orderBy(q.name.asc())
                .fetch();
    }

    @Override
    public List<Company> findAllActiveOrderByNameAsc() {
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
