package spring.fitlinkbe.domain.outbox;

import lombok.*;
import spring.fitlinkbe.domain.common.exception.CustomException;

import java.time.LocalDateTime;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.OUTBOX_IS_ALREADY_DONE;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.OUTBOX_IS_ALREADY_FAIL;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Outbox {

    private Long outboxId;

    private AggregateType aggregateType;

    private Long aggregateId;

    private String messageId;

    private EventStatus eventStatus;

    private EventType eventType;

    private String payload;

    private int retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    public void publish() {
        if (eventStatus == EventStatus.SEND_SUCCESS) {
            throw new CustomException(OUTBOX_IS_ALREADY_DONE,
                    OUTBOX_IS_ALREADY_DONE.getMsg());
        }

        if (eventStatus == EventStatus.SEND_FAIL) {
            throw new CustomException(OUTBOX_IS_ALREADY_FAIL,
                    OUTBOX_IS_ALREADY_FAIL.getMsg());
        }
        eventStatus = EventStatus.SEND_SUCCESS;
    }


    public void plusRetryCount() {
        retryCount++;
    }

    public void fail() {
        if (eventStatus == EventStatus.SEND_SUCCESS) {
            throw new CustomException(OUTBOX_IS_ALREADY_DONE,
                    OUTBOX_IS_ALREADY_DONE.getMsg());
        }

        eventStatus = EventStatus.SEND_FAIL;
    }

    public void restore() {
        eventStatus = EventStatus.INIT;
    }

    public enum AggregateType {
        RESERVATION
    }

    public enum EventStatus {
        INIT,
        SEND_SUCCESS,
        SEND_FAIL
    }

    @RequiredArgsConstructor
    @Getter
    public enum EventType {
        CREATE_FIXED_RESERVATION("고정 예약 생성");
        private final String msg;
    }

}
