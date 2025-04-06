package spring.fitlinkbe.domain.notification;

import spring.fitlinkbe.domain.common.enums.UserRole;

public interface NotificationRepository {
    Notification getNotification(Long personalDetailId);

    Notification getNotification(Long personalDetailId, Notification.NotificationType notificationType);

    Notification getNotification(Long refId, Notification.ReferenceType refType);

    Notification getNotification(Long refId, UserRole target, Notification.ReferenceType refType);

    void save(Notification notification);
}
