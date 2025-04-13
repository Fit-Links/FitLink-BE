package spring.fitlinkbe.domain.notification;


public interface FcmService {
    void sendNotification(String targetToken, String title, String body);
}
