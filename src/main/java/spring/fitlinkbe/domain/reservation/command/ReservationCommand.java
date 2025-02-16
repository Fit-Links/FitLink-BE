package spring.fitlinkbe.domain.reservation.command;

import lombok.Builder;

import java.time.LocalDateTime;

public class ReservationCommand {

    @Builder(toBuilder = true)
    public record SetDisabledTime(LocalDateTime date, Long trainerId) {

    }
}
