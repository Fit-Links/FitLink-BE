package spring.fitlinkbe.domain.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spring.fitlinkbe.domain.outbox.command.OutboxCommand;
import spring.fitlinkbe.domain.producer.EventProducer;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OutboxServiceTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private OutboxService outboxService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("outbox 생성 TEST")
    class CreateOutboxServiceTest {
        @Test
        @DisplayName("outbox 생성 - 성공")
        void createOutbox() {
            //given
            String messageId = UUID.randomUUID().toString();
            String payload = "good";

            OutboxCommand.Create command = OutboxCommand.Create.builder()
                    .aggregateId(1L)
                    .aggregateType(Outbox.AggregateType.RESERVATION)
                    .eventStatus(Outbox.EventStatus.INIT)
                    .eventType(Outbox.EventType.CREATE_FIXED_RESERVATION)
                    .messageId(messageId)
                    .payload(payload)
                    .build();

            Outbox outbox = Outbox.builder()
                    .outboxId(1L)
                    .aggregateId(1L)
                    .aggregateType(Outbox.AggregateType.RESERVATION)
                    .eventStatus(Outbox.EventStatus.INIT)
                    .eventType(Outbox.EventType.CREATE_FIXED_RESERVATION)
                    .messageId(messageId)
                    .payload(payload)
                    .build();

            when(outboxRepository.saveOutbox(any(Outbox.class))).thenReturn(Optional.ofNullable(outbox));

            //when
            Outbox result = outboxService.createOutbox(command);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getPayload()).isEqualTo(payload);
            assertThat(result.getEventStatus()).isEqualTo(Outbox.EventStatus.INIT);
        }
    }

    @Nested
    @DisplayName("outbox 발행 TEST")
    class PublishOutboxServiceTest {
        @Test
        @DisplayName("outbox 발행 - 성공")
        void createOutbox() {
            //given
            String messageId = UUID.randomUUID().toString();
            String payload = "good";

            Outbox outbox = Outbox.builder()
                    .outboxId(1L)
                    .aggregateId(1L)
                    .aggregateType(Outbox.AggregateType.RESERVATION)
                    .eventStatus(Outbox.EventStatus.INIT)
                    .eventType(Outbox.EventType.CREATE_FIXED_RESERVATION)
                    .messageId(messageId)
                    .payload(payload)
                    .build();

            when(outboxRepository.getOutbox(messageId)).thenReturn(Optional.ofNullable(outbox));

            //when
            Outbox result = outboxService.publishOutbox(messageId);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getPayload()).isEqualTo(payload);
            assertThat(result.getEventStatus()).isEqualTo(Outbox.EventStatus.SEND_SUCCESS);
        }
    }
}
