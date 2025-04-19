package spring.fitlinkbe.interfaces.controller.notification.dto;

import lombok.Builder;
import spring.fitlinkbe.domain.notification.Notification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NotificationResponseDto {

    @Builder(toBuilder = true)
    public record Summary(
            Long notificationId,
            String type,
            String content,
            LocalDateTime sendDate,
            boolean isProcessed

    ) {

        public static NotificationResponseDto.Summary of(Notification notification) {

            return Summary.builder()
                    .notificationId(notification.getNotificationId())
                    .type(notification.getRefType().getName())
                    .content(notification.getContent())
                    .sendDate(notification.getSendDate())
                    .isProcessed(notification.isProcessed())
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record Detail(
            Long notificationId,
            Long refId,
            String type,
            String content,
            LocalDateTime sendDate,
            boolean isProcessed,
            UserDetail userDetail

    ) {

        @Builder(toBuilder = true)
        public record UserDetail(String name, LocalDate birthDate, String phoneNumber,
                                 String profilePictureUrl) {
        }

        public static NotificationResponseDto.Detail of(Notification notification) {

            return Detail.builder()
                    .notificationId(notification.getNotificationId())
                    .type(notification.getRefType().getName())
                    .content(notification.getContent())
                    .sendDate(notification.getSendDate())
                    .isProcessed(notification.isProcessed())
                    .userDetail(UserDetail.builder()
                            .name(notification.getPersonalDetail().getName())
                            .birthDate(notification.getPersonalDetail().getBirthDate())
                            .phoneNumber(notification.getPersonalDetail().getPhoneNumber())
                            .profilePictureUrl(notification.getPersonalDetail().getProfilePictureUrl())
                            .build())
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record PushToken(String message) {
        public static PushToken of(String message) {
            return PushToken.builder()
                    .message(message)
                    .build();
        }
    }

}
