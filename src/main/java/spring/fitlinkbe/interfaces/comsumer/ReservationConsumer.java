package spring.fitlinkbe.interfaces.comsumer;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.application.reservation.ReservationFacade;
import spring.fitlinkbe.application.reservation.criteria.ReservationCriteria;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.producer.EventTopic;
import spring.fitlinkbe.domain.reservation.event.GenerateFixedReservationEvent;
import spring.fitlinkbe.support.utils.JsonUtils;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReservationConsumer {

    private final ReservationFacade reservationFacade;

    @SqsListener(queueNames = EventTopic.RESERVATION_QUEUE)
    public void handleReservationMessage(String message) {
        GenerateFixedReservationEvent payload = JsonUtils.toObject(message, GenerateFixedReservationEvent.class);
        if (payload == null) {
            throw new CustomException(ErrorCode.OUTBOX_PAYLOAD_INVALID);
        }
        // 일주일 뒤 예약 진행
        reservationFacade.executeCreateFixedReservation(ReservationCriteria.EventCreateFixed.builder()
                .memberId(payload.memberId())
                .trainerId(payload.trainerId())
                .sessionInfoId(payload.sessionInfoId())
                .confirmDate(payload.confirmDate())
                .build());
    }
}
