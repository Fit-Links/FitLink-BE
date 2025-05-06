package spring.fitlinkbe.domain.notification.event;

import lombok.Builder;

@Builder(toBuilder = true)
public record PushEvent(String pushToken, String name, String content) {
}
