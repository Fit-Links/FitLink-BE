package spring.fitlinkbe.infra.notification;

public interface PushManager {
    void pushNotification(String token, String title, String content);
}
