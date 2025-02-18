package spring.fitlinkbe.infra.notification;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.fitlinkbe.domain.notification.Notification;

import java.util.Optional;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    @EntityGraph(attributePaths = {"personalDetail"})
    Optional<NotificationEntity> findByPersonalDetail_PersonalDetailId(Long personalDetailId);

    @EntityGraph(attributePaths = {"personalDetail"})
    Optional<NotificationEntity> findByPersonalDetail_PersonalDetailIdAndRefType(Long personalDetailId, Notification.NotificationType type);
}
