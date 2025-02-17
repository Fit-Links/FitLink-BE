package spring.fitlinkbe.domain.reservation;

import spring.fitlinkbe.domain.common.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> getReservations();

    List<Reservation> getReservations(
            LocalDateTime startDate, LocalDateTime endDate, UserRole role, Long userId);

    List<Reservation> cancelReservations(List<Reservation> canceledReservations);

    Optional<Reservation> getReservation(Long reservationId);

    Optional<Reservation> saveReservation(Reservation reservation);


    Optional<Session> getSession(Long reservationId);

    Optional<Session> saveSession(Session session);

    List<Session> cancelSessions(List<Session> sessions);
}
