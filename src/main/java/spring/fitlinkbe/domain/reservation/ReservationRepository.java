package spring.fitlinkbe.domain.reservation;

import spring.fitlinkbe.domain.common.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {


    List<Reservation> getReservations(
            LocalDateTime startDate, LocalDateTime endDate, UserRole role, Long userId);

    Optional<Reservation> saveReservation(Reservation reservation);


}
