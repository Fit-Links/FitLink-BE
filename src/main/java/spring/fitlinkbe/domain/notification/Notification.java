package spring.fitlinkbe.domain.notification;

import lombok.*;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long notificationId;
    private Long refId;
    private NotificationType refType;
    private PersonalDetail personalDetail;
    private String name;
    private String content;
    private Boolean isSent;
    private Boolean isRead;
    private Boolean isProcessed;
    private LocalDateTime sendDate;

    @RequiredArgsConstructor
    public enum NotificationType {
        CONNECT("트레이너 연동 요청");

        private final String description;
    }
}
