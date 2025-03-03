package spring.fitlinkbe.infra.reservation;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.reservation.Session;

import java.util.List;

import static spring.fitlinkbe.infra.reservation.QSessionEntity.sessionEntity;

@RequiredArgsConstructor
public class SessionRepositoryCustomImpl implements SessionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SessionEntity> findSessions(Long memberId, Session.Status status, Pageable pageRequest) {
        List<SessionEntity> entityList = queryFactory.selectFrom(sessionEntity)
                .join(sessionEntity.reservation).fetchJoin()
                .where(eqMemberId(memberId).and(eqStatus(status)))
                .orderBy(sessionEntity.createdAt.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        Long totalCount = queryFactory.select(sessionEntity.count())
                .from(sessionEntity)
                .where(eqMemberId(memberId).and(eqStatus(status)))
                .fetchOne();
        if (totalCount == null) {
            totalCount = 0L;
        }

        return new PageImpl<>(entityList, pageRequest, totalCount);
    }

    private BooleanExpression eqMemberId(Long memberId) {
        return memberId != null ? sessionEntity.reservation.member.memberId.eq(memberId) : null;
    }

    private BooleanExpression eqStatus(Session.Status status) {
        return status != null ? sessionEntity.status.eq(status) : null;
    }
}
