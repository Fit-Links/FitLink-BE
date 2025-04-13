package spring.fitlinkbe.interfaces.controller.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import spring.fitlinkbe.application.notification.criteria.NotificationCriteria;

public class FcmRequestDto {

    @Builder(toBuilder = true)
    public record FcmTokenRequest(@NotBlank(message = "FCM 토큰은 필수값 입니다.") String fcmToken) {

        public NotificationCriteria.FcmTokenRequest toCriteria() {
            return NotificationCriteria.FcmTokenRequest.builder()
                    .fcmToken(fcmToken)
                    .build();
        }
    }
}
