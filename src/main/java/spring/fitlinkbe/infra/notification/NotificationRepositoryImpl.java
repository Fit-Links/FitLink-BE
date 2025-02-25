package spring.fitlinkbe.infra.notification;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationRepository;

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
    public Notification getNotification(Long personalDetailId, Notification.NotificationType notificationType) {
        return notificationJpaRepository.findByPersonalDetail_PersonalDetailIdAndNotificationType(personalDetailId, notificationType)
                .map(NotificationEntity::toDomain)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    @Override
    public Notification getNotification(Long refId, Notification.ReferenceType refType) {

        return notificationJpaRepository.findByRefIdAndRefType(refId, refType)
                .map(NotificationEntity::toDomain)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    @Override
    public void save(Notification notification) {
        notificationJpaRepository.save(NotificationEntity.of(notification, em));
    }
}
