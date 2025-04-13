package spring.fitlinkbe.application.notification.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.auth.command.AuthCommand;

public class NotificationCriteria {

    @Builder(toBuilder = true)
    public record FcmTokenRequest(String fcmToken) {

        public AuthCommand.FcmTokenRequest toCommand() {
            return AuthCommand.FcmTokenRequest.builder()
                    .fcmToken(fcmToken)
                    .build();
        }
    }

}
