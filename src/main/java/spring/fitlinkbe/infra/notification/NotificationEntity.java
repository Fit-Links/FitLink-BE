package spring.fitlinkbe.infra.notification;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.infra.common.personaldetail.PersonalDetailEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private Long refId;

    @Enumerated(EnumType.STRING)
    private Notification.NotificationType refType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_detail_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PersonalDetailEntity personalDetail;

    private String name;

    private String content;

    private Boolean isSent;

    private Boolean isRead;

    private Boolean isProcessed;

    private LocalDateTime sendDate;

    public static NotificationEntity from(Notification notification) {
        return NotificationEntity.builder()
                .notificationId(notification.getNotificationId())
                .refId(notification.getRefId())
                .refType(notification.getRefType())
                .personalDetail(PersonalDetailEntity.from(notification.getPersonalDetail()))
                .name(notification.getName())
                .content(notification.getContent())
                .isSent(notification.getIsSent())
                .isRead(notification.getIsRead())
                .isProcessed(notification.getIsProcessed())
                .sendDate(notification.getSendDate())
                .build();
    }

    public Notification toDomain() {
        return Notification.builder()
                .notificationId(notificationId)
                .refId(refId)
                .refType(refType)
                .personalDetail(personalDetail.toDomain())
                .name(name)
                .content(content)
                .isSent(isSent)
                .isRead(isRead)
                .isProcessed(isProcessed)
                .sendDate(sendDate)
                .build();
    }

}
