package spring.fitlinkbe.infra.producer;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class EventTopic {

    @Value("${spring.cloud.aws.sqs.queue-name}")
    private String reservationQueue;
}
