package spring.fitlinkbe.infra.notification;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.notification.Notification;

import java.util.Optional;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long>, NotificationRepositoryCustom {

    @EntityGraph(attributePaths = {"personalDetail"})
    Optional<NotificationEntity> findByPersonalDetail_PersonalDetailId(Long personalDetailId);

    @EntityGraph(attributePaths = {"personalDetail"})
    Optional<NotificationEntity> findByPersonalDetail_PersonalDetailIdAndNotificationType(Long personalDetailId, Notification.NotificationType type);

    @EntityGraph(attributePaths = {"personalDetail"})
    Optional<NotificationEntity> findByRefIdAndRefType(Long refId, Notification.ReferenceType type);

    @EntityGraph(attributePaths = {"personalDetail"})
    Optional<NotificationEntity> findByRefIdAndRefTypeAndTarget(Long refId, Notification.ReferenceType type, UserRole target);
}
