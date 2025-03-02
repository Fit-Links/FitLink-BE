package spring.fitlinkbe.domain.reservation;

import spring.fitlinkbe.domain.common.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> getReservations();

    List<Reservation> getReservations(UserRole role, Long userId);

    List<Reservation> cancelReservations(List<Reservation> canceledReservations);

    Optional<Reservation> getReservation(Long reservationId);

    Optional<Reservation> reserveSession(Reservation reservations);

    Optional<Session> getSession(Long reservationId);

    Optional<Session> createSession(Session session);

    List<Session> cancelSessions(List<Session> sessions);

    List<Session> createSessions(List<Session> sessions);


}
