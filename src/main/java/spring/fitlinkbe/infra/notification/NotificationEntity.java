package spring.fitlinkbe.infra.notification;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.infra.common.personaldetail.PersonalDetailEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "notification")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private Long refId;

    @Enumerated(EnumType.STRING)
    private Notification.ReferenceType refType;

    @Enumerated(EnumType.STRING)
    private UserRole target;

    private Long partnerId;

    @Enumerated(EnumType.STRING)
    private Notification.NotificationType notificationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_detail_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private PersonalDetailEntity personalDetail;

    private String name;

    private String content;

    private boolean isSent;

    private boolean isProcessed;

    private LocalDateTime sendDate;

    public static NotificationEntity of(Notification notification, EntityManager em) {
        return NotificationEntity.builder()
                .notificationId(notification.getNotificationId())
                .refId(notification.getRefId())
                .refType(notification.getRefType())
                .target(notification.getTarget())
                .notificationType(notification.getNotificationType())
                .personalDetail(PersonalDetailEntity.of(notification.getPersonalDetail(), em))
                .partnerId(notification.getPartnerId())
                .name(notification.getName())
                .content(notification.getContent())
                .isSent(notification.isSent())
                .isProcessed(notification.isProcessed())
                .sendDate(notification.getSendDate())
                .build();
    }

    public Notification toDomain() {
        return Notification.builder()
                .notificationId(notificationId)
                .refId(refId)
                .refType(refType)
                .target(target)
                .notificationType(notificationType)
                .personalDetail(personalDetail.toDomain())
                .partnerId(partnerId)
                .name(name)
                .content(content)
                .isSent(isSent)
                .isProcessed(isProcessed)
                .sendDate(sendDate)
                .build();
    }

}
