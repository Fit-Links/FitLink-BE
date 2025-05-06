package spring.fitlinkbe.domain.reservation.event;

import lombok.Builder;
import spring.fitlinkbe.domain.outbox.Outbox;
import spring.fitlinkbe.domain.outbox.OutboxEvent;
import spring.fitlinkbe.domain.outbox.command.OutboxCommand;
import spring.fitlinkbe.support.utils.JsonUtils;

import java.time.LocalDateTime;


@Builder(toBuilder = true)
public record GenerateFixedReservationEvent(
        Long reservationId,
        String messageId,
        Long trainerId,
        Long memberId,
        Long sessionInfoId,
        String name,
        LocalDateTime confirmDate,
        String topic
) implements OutboxEvent {

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getKey() {
        return reservationId.toString();
    }

    @Override
    public String toPayload() {
        return JsonUtils.toJson(this);
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public OutboxCommand.Create toOutboxCommand() {
        return new OutboxCommand.Create(Outbox.AggregateType.RESERVATION, reservationId, messageId, Outbox.EventStatus.INIT,
                Outbox.EventType.CREATE_FIXED_RESERVATION, toPayload());
    }

}
