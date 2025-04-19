package spring.fitlinkbe.infra.notification;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;
    private final EntityManager em;

    @Override
    public Notification getNotification(Long personalDetailId) {
        return notificationJpaRepository.findByPersonalDetail_PersonalDetailId(personalDetailId)
                .map(NotificationEntity::toDomain)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    @Override
    public Notification getNotification(Long personalDetailId, Long notificationId) {
        return notificationJpaRepository.findByNotificationIdAndPersonalDetail_PersonalDetailId(notificationId,
                        personalDetailId).map(NotificationEntity::toDomain)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    @Override
    public Notification getNotification(Long personalDetailId, Notification.NotificationType notificationType) {
        return notificationJpaRepository.findByPersonalDetail_PersonalDetailIdAndNotificationType(personalDetailId, notificationType)
                .map(NotificationEntity::toDomain)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    @Override
    public List<Notification> getNotification(Long refId, Notification.ReferenceType refType) {

        return notificationJpaRepository.findByRefIdAndRefType(refId, refType)
                .stream().map(NotificationEntity::toDomain)
                .toList();

    }

    @Override
    public List<Notification> getNotification(Long refId, UserRole target, Notification.ReferenceType refType) {
        return notificationJpaRepository.findByRefIdAndRefTypeAndTarget(refId, refType, target)
                .stream().map(NotificationEntity::toDomain)
                .toList();
    }

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(NotificationEntity.of(notification, em))
                .toDomain();
    }

    @Override
    public Page<Notification> getNotifications(Notification.ReferenceType type, Pageable pageRequest, UserRole userRole,
                                               Long userId, String keyword) {
        Page<NotificationEntity> notifications = notificationJpaRepository
                .findNotifications(type, pageRequest, userRole, userId, keyword);

        return notifications.map(NotificationEntity::toDomain);

    }
}
