package spring.fitlinkbe.application.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.notification.command.NotificationCommand;
import spring.fitlinkbe.support.security.SecurityUser;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;

    public Page<Notification> getNotifications(Notification.ReferenceType type, Pageable pageRequest,
                                               SecurityUser user, String keyword) {

        NotificationCommand.GetNotifications command = NotificationCommand.GetNotifications.builder()
                .type(type)
                .pageRequest(pageRequest)
                .keyword(keyword)
                .build();

        return notificationService.getNotifications(command, user);
    }

    public Notification getNotificationDetail(Long notificationId, SecurityUser user) {
        return notificationService.getNotificationDetail(notificationId, user);
    }

}
