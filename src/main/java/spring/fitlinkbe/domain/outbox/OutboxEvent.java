package spring.fitlinkbe.domain.outbox;

import spring.fitlinkbe.domain.outbox.command.OutboxCommand;

public interface OutboxEvent {
    String getTopic();

    String getKey();

    String toPayload();

    String getMessageId();

    OutboxCommand.Create toOutboxCommand();
}
