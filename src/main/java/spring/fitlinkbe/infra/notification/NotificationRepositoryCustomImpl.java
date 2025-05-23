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
                                                      Long partnerId, Long personalDetailId, String keyword) {

        List<NotificationEntity> notifications = queryFactory.selectFrom(notificationEntity)
                .leftJoin(personalDetailEntity)
                .on(personalDetailEntity.personalDetailId.eq(personalDetailId))
                .where(
                        eqRefType(type),
                        eqUserRole(userRole),
                        eqPersonalDetailId(personalDetailId),
                        eqPartnerId(partnerId),
                        likeKeyword(keyword)
                )
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch();

        Long totalCount = queryFactory.select(notificationEntity.count())
                .from(notificationEntity)
                .where(
                        eqRefType(type),
                        eqUserRole(userRole),
                        eqPersonalDetailId(personalDetailId),
                        eqPartnerId(partnerId),
                        likeKeyword(keyword)
                )
                .fetchOne();

        if (totalCount == null) {
            totalCount = 0L;
        }

        return new PageImpl<>(notifications, pageRequest, totalCount);
    }

    private BooleanExpression likeKeyword(String keyword) {
        return keyword != null ? notificationEntity.content.containsIgnoreCase(keyword)
                .or(notificationEntity.name.containsIgnoreCase(keyword)) : null;
    }

    private static BooleanExpression eqPersonalDetailId(Long targetUserId) {
        return notificationEntity.personalDetail.personalDetailId.eq(targetUserId);
    }

    private BooleanExpression eqPartnerId(Long partnerId) {
        return partnerId != null ? notificationEntity.partnerId.eq(partnerId) : null;
    }

    private static BooleanExpression eqUserRole(UserRole userRole) {
        return notificationEntity.target.eq(userRole);
    }

    private static BooleanExpression eqRefType(Notification.ReferenceType type) {
        return type != null ? notificationEntity.refType.eq(type) : null;
    }
}
