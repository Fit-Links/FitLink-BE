package spring.fitlinkbe.infra.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.notification.client.PushNotificationClient;
import spring.fitlinkbe.infra.notification.PushManager;

@Component
@RequiredArgsConstructor
public class PushNotificationClientImpl implements PushNotificationClient {

    private final PushManager pushManager;

    @Override
    public void pushNotification(String token, String title, String content) {
        pushManager.pushNotification(token, title, content);
    }
}
