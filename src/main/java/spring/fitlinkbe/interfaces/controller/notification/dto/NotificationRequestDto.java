package spring.fitlinkbe.interfaces.controller.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import spring.fitlinkbe.application.notification.criteria.NotificationCriteria;

public class NotificationRequestDto {

    @Builder(toBuilder = true)
    public record PushTokenRequest(@NotBlank(message = "푸쉬 토큰은 필수값 입니다.") String pushToken) {

        public NotificationCriteria.PushTokenRequest toCriteria() {
            return NotificationCriteria.PushTokenRequest.builder()
                    .pushToken(pushToken)
                    .build();
        }
    }
}
