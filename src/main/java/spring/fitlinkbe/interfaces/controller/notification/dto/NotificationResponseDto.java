package spring.fitlinkbe.interfaces.controller.notification.dto;

import lombok.Builder;
import spring.fitlinkbe.domain.notification.Notification;

import java.time.LocalDateTime;

public class NotificationResponseDto {
    @Builder(toBuilder = true)
    public record Summary(
            Long notificationId,
            String content,
            LocalDateTime sendDate,
            boolean isProcessed

    ) {

        public static NotificationResponseDto.Summary of(Notification notification) {

            return Summary.builder()
                    .notificationId(notification.getNotificationId())
                    .content(notification.getContent())
                    .sendDate(notification.getSendDate())
                    .isProcessed(notification.isProcessed())
                    .build();
        }

    }

}
