package spring.fitlinkbe.interfaces.controller.notification.dto;

import lombok.Builder;
import spring.fitlinkbe.application.notification.criteria.NotificationResult;
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
        public record UserDetail(Long userId, String name, LocalDate birthDate, String phoneNumber,
                                 String profilePictureUrl) {
        }

        public static NotificationResponseDto.Detail of(NotificationResult.NotificationDetail notificationDetail) {

            return Detail.builder()
                    .notificationId(notificationDetail.notification().getNotificationId())
                    .refId(notificationDetail.notification().getRefId())
                    .type(notificationDetail.notification().getRefType().getName())
                    .content(notificationDetail.notification().getContent())
                    .sendDate(notificationDetail.notification().getSendDate())
                    .isProcessed(notificationDetail.notification().isProcessed())
                    .userDetail(UserDetail.builder()
                            .userId(notificationDetail.personalDetail().getUserId())
                            .name(notificationDetail.personalDetail().getName())
                            .birthDate(notificationDetail.personalDetail().getBirthDate())
                            .phoneNumber(notificationDetail.personalDetail().getPhoneNumber())
                            .profilePictureUrl(notificationDetail.personalDetail().getProfilePictureUrl())
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
