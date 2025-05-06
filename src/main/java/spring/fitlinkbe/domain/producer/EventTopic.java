package spring.fitlinkbe.domain.producer;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class EventTopic {
    public static final String RESERVATION_QUEUE = "reservation-queue";
}
