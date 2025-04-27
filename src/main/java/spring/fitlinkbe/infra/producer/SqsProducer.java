package spring.fitlinkbe.infra.producer;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsProducer {

    private final SqsTemplate sqsTemplate;

    public void publish(String topic, String payload) {
        log.info("[SQS] :: PUBLISH :: sending to queue={}, payload={}", topic, payload);

        try {
            sqsTemplate.send(to -> to.queue(topic).payload(payload));
            log.info("[SQS] :: SUCCESS :: queue={}, payload={}", topic, payload);
        } catch (Exception ex) {
            log.error("[SQS] :: FAILED :: queue={}, payload={}, error={}", topic, payload, ex.getMessage(), ex);
        }
    }
}
