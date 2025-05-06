package spring.fitlinkbe.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.notification.client.PushNotificationClient;
import spring.fitlinkbe.domain.notification.command.NotificationCommand;
import spring.fitlinkbe.domain.notification.command.NotificationRequest;
import spring.fitlinkbe.domain.notification.event.PushEvent;
import spring.fitlinkbe.support.security.SecurityUser;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationStrategyHandler strategyHandler;
    private final PushNotificationClient pushNotificationClient;
    private final ApplicationEventPublisher publisher;

    public Page<Notification> getNotifications(NotificationCommand.GetNotifications command, SecurityUser user) {

        return notificationRepository.getNotifications(command.type(), command.pageRequest(),
                user.getUserRole(), user.getPersonalDetailId(), command.keyword());
    }

    public Notification getNotificationDetail(Long notificationId, SecurityUser user) {
        return notificationRepository.getNotification(notificationId, user.getPersonalDetailId());
    }

    @Transactional
    public <T extends NotificationRequest> void sendNotification(T request) {
        // 1. DB 저장
        Notification notification = strategyHandler.handle(request);
        notificationRepository.save(notification);
        // 2. push 알림 전송 이벤트로 전달
        publisher.publishEvent(PushEvent.builder()
                .pushToken(request.getPushToken())
                .name(notification.getName())
                .content(notification.getContent())
                .build());
    }

    public void pushNotification(String token, String title, String content) {
        pushNotificationClient.pushNotification(token, title, content);
    }
}
