package spring.fitlinkbe.domain.notification;

public interface NotificationRepository {
    Notification getNotification(Long personalDetailId);

    void save(Notification notification);
}
