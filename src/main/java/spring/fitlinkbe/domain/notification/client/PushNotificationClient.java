package spring.fitlinkbe.domain.notification.client;

public interface PushNotificationClient {

    void pushNotification(String token, String title, String content);
}
