package spring.fitlinkbe.domain.reservation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> getReservations();

    List<Reservation> getFixedReservations();

    List<Reservation> getFixedReservations(Long memberId);

    List<Reservation> getFixedReservations(Long trainerId, LocalDateTime fixedReservationDate);

    List<Reservation> getReservationsWithWaitingStatus(Long trainerId);

    List<Reservation> getReservations(UserRole role, Long userId);

    List<Reservation> saveReservations(List<Reservation> canceledReservations);

    List<Session> saveSessions(List<Session> sessions);

    Optional<Reservation> getReservation(Long reservationId);

    Optional<Reservation> getReservation(Long reservationId, Long trainerId);

    Optional<Reservation> saveReservation(Reservation reservation);

    void deleteReservation(Reservation reservation);

    Optional<Session> getSession(Long reservationId);

    Page<Session> getSessions(Long memberId, Long trainerId, Session.Status status, Pageable pageRequest);

    Optional<Session> saveSession(Session session);

    boolean isConfirmedReservationExists(Long trainerId, List<LocalDate> dates);

    boolean isConfirmedReservationsExists(Long trainerId, List<LocalDateTime> dates);

    boolean isConfirmedReservationExists(Long trainerId, LocalDateTime checkDate);
}
