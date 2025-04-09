package spring.fitlinkbe.infra.notification;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.notification.Notification;

import java.util.List;

import static spring.fitlinkbe.infra.common.personaldetail.QPersonalDetailEntity.personalDetailEntity;
import static spring.fitlinkbe.infra.notification.QNotificationEntity.notificationEntity;

@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<NotificationEntity> findNotifications(Notification.ReferenceType type, Pageable pageRequest, UserRole userRole,
                                                      Long personalDetailId) {

        List<NotificationEntity> notifications = queryFactory.selectFrom(notificationEntity)
                .leftJoin(personalDetailEntity)
                .on(personalDetailEntity.personalDetailId.eq(personalDetailId))
                .where(
                        eqRefType(type),
                        eqUserRole(userRole),
                        eqPersonalDetailId(personalDetailId)
                )
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        Long totalCount = queryFactory.select(notificationEntity.count())
                .from(notificationEntity)
                .where(
                        eqRefType(type),
                        eqUserRole(userRole),
                        eqPersonalDetailId(personalDetailId)
                )
                .fetchOne();

        if (totalCount == null) {
            totalCount = 0L;
        }

        return new PageImpl<>(notifications, pageRequest, totalCount);
    }

    private static BooleanExpression eqPersonalDetailId(Long personalDetailId) {
        return notificationEntity.personalDetail.personalDetailId.eq(personalDetailId);
    }

    private static BooleanExpression eqUserRole(UserRole userRole) {
        return notificationEntity.target.eq(userRole);
    }

    private static BooleanExpression eqRefType(Notification.ReferenceType type) {
        return type != null ? notificationEntity.refType.eq(type) : null;
    }
}
