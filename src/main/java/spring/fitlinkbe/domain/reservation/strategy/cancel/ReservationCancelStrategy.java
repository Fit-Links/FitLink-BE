package spring.fitlinkbe.domain.reservation.strategy.cancel;

import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;

public interface ReservationCancelStrategy {
    boolean supports(UserRole userRole);

    Reservation cancel(Reservation reservation, ReservationCommand.Cancel command);
}
