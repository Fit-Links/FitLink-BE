package spring.fitlinkbe.application.notification.criteria;

import lombok.Builder;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.notification.Notification;

public class NotificationCriteria {

    @Builder(toBuilder = true)
    public record SearchCondition(Notification.ReferenceType type, Pageable pageRequest,
                                  String q, Long memberId) {
    }

    @Builder(toBuilder = true)
    public record PushTokenRequest(String pushToken) {
        public AuthCommand.PushTokenRequest toCommand() {
            return AuthCommand.PushTokenRequest.builder()
                    .pushToken(pushToken)
                    .build();
        }
    }
}
