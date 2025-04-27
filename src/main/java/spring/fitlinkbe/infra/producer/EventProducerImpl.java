package spring.fitlinkbe.infra.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.producer.EventProducer;

@Component
@RequiredArgsConstructor
public class EventProducerImpl implements EventProducer {

    private final SqsProducer sqsProducer;

    @Override
    public void publish(String topic, String key, String payload) {
        sqsProducer.publish(topic, payload);
    }
}
