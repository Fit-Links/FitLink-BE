package spring.fitlinkbe.infra.outbox;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import spring.fitlinkbe.domain.outbox.Outbox;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "outbox")
public class OutboxEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxId;

    @Enumerated(EnumType.STRING)
    private Outbox.AggregateType aggregateType;

    private Long aggregateId;

    private String messageId;

    @Enumerated(EnumType.STRING)
    private Outbox.EventStatus eventStatus;

    @Enumerated(EnumType.STRING)
    private Outbox.EventType eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private int retryCount;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    public static OutboxEntity toEntity(Outbox outbox) {
        return OutboxEntity.builder()
                .outboxId(outbox.getOutboxId())
                .aggregateType(outbox.getAggregateType())
                .aggregateId(outbox.getAggregateId())
                .messageId(outbox.getMessageId())
                .eventStatus(outbox.getEventStatus())
                .eventType(outbox.getEventType())
                .payload(outbox.getPayload())
                .retryCount(outbox.getRetryCount())
                .createdAt(outbox.getCreatedAt())
                .sentAt(outbox.getSentAt())
                .build();
    }

    public Outbox toDomain() {
        return Outbox.builder()
                .outboxId(outboxId)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .messageId(messageId)
                .eventStatus(eventStatus)
                .eventType(eventType)
                .payload(payload)
                .retryCount(retryCount)
                .createdAt(createdAt)
                .sentAt(sentAt)
                .build();
    }
}
