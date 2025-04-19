package spring.fitlinkbe.infra.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FcmPushManager implements PushManager {

    @Override
    public void pushNotification(String token, String title, String content) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(content)
                        .build())
                .build();
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error(e.getMessage());
        }
    }
}
