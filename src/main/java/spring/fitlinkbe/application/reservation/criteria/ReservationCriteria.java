package spring.fitlinkbe.application.reservation.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;

import java.time.LocalDateTime;

public class ReservationCriteria {

    @Builder(toBuilder = true)
    public record SetDisabledTime(LocalDateTime date, Long trainerId) {

        public ReservationCommand.SetDisabledTime toCommand() {
            return ReservationCommand.SetDisabledTime.builder()
                    .trainerId(trainerId)
                    .date(date)
                    .build();
        }
    }

}
