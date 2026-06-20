package com.ticket.support_management_system_api.domain.position;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PositionRepositoryImpl implements PositionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QPosition q = QPosition.position;

    @Override
    public Optional<Position> findByName(String name) {
        return Optional.ofNullable(
                queryFactory.selectFrom(q)
                        .where(q.name.eq(name))
                        .fetchOne()
        );
    }

    @Override
    public List<Position> findAllOrderByNameAsc() {
        return queryFactory.selectFrom(q)
                .orderBy(q.name.asc())
                .fetch();
    }

    @Override
    public List<Position> findAllActiveOrderByNameAsc() {
        return queryFactory.selectFrom(q)
                .where(q.archivedAt.isNull())
                .orderBy(q.name.asc())
                .fetch();
    }

    @Override
    public Page<Position> findAllActive(Pageable pageable) {
        Long total = queryFactory
                .select(q.id.count())
                .from(q)
                .where(q.archivedAt.isNull())
                .fetchOne();

        List<Position> content = queryFactory.selectFrom(q)
                .where(q.archivedAt.isNull())
                .orderBy(q.name.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
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
