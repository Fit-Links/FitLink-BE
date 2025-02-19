package spring.fitlinkbe.domain.notification;

public interface NotificationRepository {
    Notification getNotification(Long personalDetailId);

    Notification getNotification(Long personalDetailId, Notification.NotificationType notificationType);

    void save(Notification notification);
}
