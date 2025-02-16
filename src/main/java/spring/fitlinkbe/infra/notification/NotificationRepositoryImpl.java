package spring.fitlinkbe.infra.notification;

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

    @Override
    public Notification getNotification(Long personalDetailId) {
        return notificationJpaRepository.findByPersonalDetail_PersonalDetailId(personalDetailId)
                .map(NotificationEntity::toDomain)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    @Override
    public void save(Notification notification) {
        notificationJpaRepository.save(NotificationEntity.from(notification));
    }
}
