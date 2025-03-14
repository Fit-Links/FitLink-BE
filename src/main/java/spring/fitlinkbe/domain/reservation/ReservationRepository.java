package spring.fitlinkbe.domain.reservation;

import spring.fitlinkbe.domain.common.enums.UserRole;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> getReservations();

    List<Reservation> getFixedReservations();

    List<Reservation> getReservationsWithWaitingStatus(Long trainerId);

    List<Reservation> getReservations(UserRole role, Long userId);

    List<Reservation> saveReservations(List<Reservation> canceledReservations);

    List<Session> saveSessions(List<Session> sessions);

    Optional<Reservation> getReservation(Long reservationId);

    Optional<Reservation> saveReservation(Reservation reservation);

    Optional<Session> getSession(Long reservationId);

    Optional<Session> saveSession(Session session);


}
