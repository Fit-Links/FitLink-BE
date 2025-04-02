package spring.fitlinkbe.infra.reservation;

import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import spring.fitlinkbe.domain.reservation.Reservation;

import java.time.LocalDate;
import java.util.List;

import static spring.fitlinkbe.infra.reservation.QReservationEntity.reservationEntity;


@RequiredArgsConstructor
public class ReservationRepositoryCustomImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean isConfirmedReservationExists(Long trainerId, List<LocalDate> dates) {
        // confirmDate 의 날짜 부분만 추출하는 expression 생성
        DateExpression<LocalDate> confirmDateOnly = Expressions.dateTemplate(
                LocalDate.class, "DATE({0})", reservationEntity.confirmDate);

        return queryFactory
                .selectOne() // 필요한 값만 조회하여 불필요한 데이터 로드를 방지
                .from(reservationEntity)
                .where(
                        reservationEntity.trainer.trainerId.eq(trainerId)
                                .and(confirmDateOnly.in(dates))
                                .and(reservationEntity.status.eq(Reservation.Status.RESERVATION_APPROVED))
                )
                .fetchFirst() != null;
    }
}
