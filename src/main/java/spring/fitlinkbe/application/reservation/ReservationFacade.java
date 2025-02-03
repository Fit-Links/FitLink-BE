package spring.fitlinkbe.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationService;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.util.List;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;
import static spring.fitlinkbe.domain.common.enums.UserRole.TRAINER;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;

    @Transactional(readOnly = true)
    public List<Reservation> getReservations(LocalDate date, SecurityUser user) {

        UserRole role = user.getTrainerId() == null ? MEMBER : TRAINER;
        Long userId = user.getTrainerId() == null ? user.getMemberId() : user.getTrainerId();

        return reservationService.getReservations(date, role, userId);

    }
}
