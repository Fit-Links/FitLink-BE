package spring.fitlinkbe.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationService;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;

    public List<Reservation> getReservations(LocalDate date, SecurityUser user) {

        return reservationService.getReservations(date, user.getUserRole(), user.getUserId());
    }
}
