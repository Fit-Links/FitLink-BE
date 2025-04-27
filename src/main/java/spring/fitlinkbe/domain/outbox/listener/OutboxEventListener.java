package spring.fitlinkbe.domain.outbox.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import spring.fitlinkbe.domain.common.event.OutboxEvent;
import spring.fitlinkbe.domain.outbox.OutboxService;
import spring.fitlinkbe.domain.producer.EventProducer;

@Component
@RequiredArgsConstructor
public class OutboxEventListener {

    private final OutboxService outboxService;
    private final EventProducer eventProducer;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveOutbox(OutboxEvent event) {
        // Outbox data 생성
        outboxService.createOutbox(event.toOutboxCommand());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishOutbox(OutboxEvent event) {
        // outbox 메시지 발행 완료 채크
        outboxService.publishOutbox(event.getMessageId());
        // 이벤트 메시지 발행
        eventProducer.publish(event.getTopic(), event.getKey(), event.toPayload());
    }
}
