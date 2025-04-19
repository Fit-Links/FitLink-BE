package spring.fitlinkbe.application.notification.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.auth.command.AuthCommand;

public class NotificationCriteria {

    @Builder(toBuilder = true)
    public record PushTokenRequest(String pushToken) {

        public AuthCommand.PushTokenRequest toCommand() {
            return AuthCommand.PushTokenRequest.builder()
                    .pushToken(pushToken)
                    .build();
        }
    }

}
