package spring.fitlinkbe.infra.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.notification.Notification;

public interface NotificationRepositoryCustom {

    Page<NotificationEntity> findNotifications(Notification.ReferenceType type, Pageable pageRequest,
                                               UserRole userRole, Long personalDetailId, String keyword);
}
