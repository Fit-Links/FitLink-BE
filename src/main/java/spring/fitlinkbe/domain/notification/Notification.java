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

    public static Notification connectRequestNotification(PersonalDetail trainerDetail,
                                                          String memberName, Long connectingInfoId) {
        String content = memberName + " 님에게 연동 요청이 왔습니다.";

        return Notification.builder()
                .refId(connectingInfoId)
                .refType(NotificationType.CONNECT)
                .personalDetail(trainerDetail)
                .name(NotificationType.CONNECT.getName())
                .content(content)
                .isSent(true)
                .isRead(false)
                .isProcessed(false)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification disconnectNotification(String memberName, Long trainerId) {
        String content = memberName + " 님과의 연동이 해제되었습니다.";

        return Notification.builder()
                .refId(trainerId)
                .refType(NotificationType.DISCONNECT)
                .name(NotificationType.DISCONNECT.getName())
                .content(content)
                .isSent(true)
                .isRead(false)
                .isProcessed(false)
                .sendDate(LocalDateTime.now())
                .build();
    }

    @RequiredArgsConstructor
    @Getter
    public enum NotificationType {
        CONNECT("트레이너 연동 요청", "트레이너와 연동 요청이 왔습니다."),
        DISCONNECT("트레이너 연동 해제", "회원과 연동이 해제되었습니다."),
        ;

        private final String description;
        private final String name;
    }
}
