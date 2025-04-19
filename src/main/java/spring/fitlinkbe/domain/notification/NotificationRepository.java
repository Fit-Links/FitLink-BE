package spring.fitlinkbe.domain.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.enums.UserRole;

import java.util.List;

public interface NotificationRepository {
    Notification getNotification(Long personalDetailId);

    Notification getNotification(Long personalDetailId, Long notificationId);

    Notification getNotification(Long personalDetailId, Notification.NotificationType notificationType);

    List<Notification> getNotification(Long refId, Notification.ReferenceType refType);

    List<Notification> getNotification(Long refId, UserRole target, Notification.ReferenceType refType);

    Notification save(Notification notification);

    Page<Notification> getNotifications(Notification.ReferenceType type, Pageable pageRequest, UserRole userRole,
                                        Long personalDetailId, String keyword);

}
