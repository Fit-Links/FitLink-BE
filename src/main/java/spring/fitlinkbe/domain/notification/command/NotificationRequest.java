package spring.fitlinkbe.domain.notification.command;

import spring.fitlinkbe.domain.notification.Notification;

public interface NotificationRequest {
    Notification.NotificationType getType();
}