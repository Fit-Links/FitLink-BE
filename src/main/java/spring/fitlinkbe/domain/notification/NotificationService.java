package spring.fitlinkbe.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.notification.command.NotificationRequest;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationStrategyHandler strategyHandler;

    public <T extends NotificationRequest> void sendNotification(T request) {
        Notification notification = strategyHandler.handle(request);
        notificationRepository.save(notification);
    }

}
