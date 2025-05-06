package spring.fitlinkbe.domain.notification.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.notification.event.PushEvent;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPushEvent(PushEvent event) {
        notificationService.pushNotification(event.pushToken(), event.name(), event.content());
    }
}
