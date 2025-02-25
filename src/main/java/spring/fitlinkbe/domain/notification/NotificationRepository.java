package spring.fitlinkbe.domain.notification;

public interface NotificationRepository {
    Notification getNotification(Long personalDetailId);

    Notification getNotification(Long personalDetailId, Notification.NotificationType notificationType);

    Notification getNotification(Long refId, Notification.ReferenceType refType);

    void save(Notification notification);
}
