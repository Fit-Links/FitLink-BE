package spring.fitlinkbe.domain.reservation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.RESERVATION_WAITING_MEMBERS_EMPTY;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.SESSION_CREATE_FAILED;
import static spring.fitlinkbe.domain.reservation.Reservation.Status;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.DISABLED_TIME_RESERVATION;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_WAITING;
import static spring.fitlinkbe.domain.reservation.Reservation.getEndDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<Reservation> getReservations() {

        return reservationRepository.getReservations();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservations(LocalDate date, UserRole role, Long userId) {

        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = getEndDate(startDate, role);

        List<Reservation> reservations = reservationRepository.getReservations(role, userId);

        return reservations.stream()
                .filter(reservation -> reservation.isReservationInRange(startDate, endDate))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsWithWaitingStatus(Long trainerId) {
        List<Reservation> WaitingMembers = reservationRepository.getReservationsWithWaitingStatus(RESERVATION_WAITING,
                trainerId);

        if (WaitingMembers.isEmpty()) {
            throw new CustomException(RESERVATION_WAITING_MEMBERS_EMPTY);
        }

        return WaitingMembers;
    }

    @Transactional(readOnly = true)
    public Reservation getReservation(Long reservationId) {
        return reservationRepository.getReservation(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservationId)));
    }

    @Transactional(readOnly = true)
    public Session getSession(Status status, Long reservationId) {
        Optional<Session> getSession = reservationRepository.getSession(reservationId);
        // 예약 승낙 전에는 세션 정보는 없다.
        if (status == RESERVATION_WAITING) {
            return null;
        }

        return getSession.orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                "세션 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservationId)));
    }

    @Transactional
    public void cancelReservations(List<Reservation> reservations, String message) {
        // 예약 정보 취소
        reservations.forEach(reservation -> reservation.cancel(message));
        // 취소한 예약 정보 저장
        reservationRepository.cancelReservations(reservations);
        // 세션 정보 찾기
        List<Session> sessions = reservations.stream()
                .map(reservation -> reservationRepository.getSession(reservation.getReservationId()))
                .flatMap(Optional::stream)
                .toList();
        //세션 정보 취소
        sessions.forEach(session -> session.cancel(message));
        // 세션 취소 정보 저장
        reservationRepository.cancelSessions(sessions);
    }

    @Transactional
    public Reservation setDisabledTime(ReservationCommand.SetDisabledTime command, Trainer trainerInfo) {

        Reservation reservation = Reservation.builder()
                .trainer(trainerInfo)
                .reservationDates(List.of(command.date()))
                .status(DISABLED_TIME_RESERVATION)
                .build();

        return reservationRepository.reserveSession(reservation).orElseThrow(() ->
                new CustomException(ErrorCode.SET_DISABLE_DATE_FAILED,
                        "예약 불가 설정을 할 수 없습니다."));
    }

    @Transactional
    public Reservation reserveSession(Reservation reservation) {
        return reservationRepository.reserveSession(reservation)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.RESERVATION_IS_FAILED,
                                "예약에 실패하였습니다."));
    }

    @Transactional
    public Session createSession(Reservation savedReservation) {

        Session session = Session.builder()
                .reservationId(savedReservation.getReservationId())
                .status(Session.Status.SESSION_WAITING)
                .build();

        return reservationRepository.createSession(session)
                .orElseThrow(() -> new CustomException(SESSION_CREATE_FAILED));
    }


}
