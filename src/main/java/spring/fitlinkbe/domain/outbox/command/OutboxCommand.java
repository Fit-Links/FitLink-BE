package spring.fitlinkbe.domain.outbox.command;

import spring.fitlinkbe.domain.outbox.Outbox;


public class OutboxCommand {
    public record Create(
            Outbox.AggregateType aggregateType,
            Long aggregateId,
            String messageId,
            Outbox.EventStatus eventStatus,
            Outbox.EventType eventType,
            String payload) {

        public Outbox toDomain() {
            return Outbox.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .messageId(messageId)
                    .eventStatus(eventStatus)
                    .eventType(eventType)
                    .payload(payload)
                    .build();
        }
    }
}
