package spring.fitlinkbe.domain.reservation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.RESERVATION_WAITING_MEMBERS_EMPTY;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.SESSION_CREATE_FAILED;
import static spring.fitlinkbe.domain.reservation.Reservation.Status;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.DISABLED_TIME_RESERVATION;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_WAITING;
import static spring.fitlinkbe.domain.reservation.Reservation.getEndDate;
import static spring.fitlinkbe.domain.reservation.Session.Status.SESSION_WAITING;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public List<Reservation> getReservations() {

        return reservationRepository.getReservations()
                .stream().filter(Reservation::isAlreadyCancel)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationThatTimes(ReservationCommand.GetReservationThatTimes
                                                             command) {
        List<Reservation> reservations = reservationRepository.getReservations(UserRole.TRAINER, command.trainerId());

        // 해당 시간의 예약만 리턴
        return reservations.stream()
                .filter((reservation) -> reservation.isReservationDateSame(command.date()))
                .filter(Reservation::isAlreadyCancel) // 이미 취소된 예약인지 확인
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservations(ReservationCommand.GetReservations command) {

        LocalDateTime startDate = command.date().atStartOfDay();
        LocalDateTime endDate = getEndDate(startDate, command.role());

        List<Reservation> reservations = reservationRepository.getReservations(command.role(), command.userId());

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

    @Transactional(readOnly = true)
    public Page<Session> getSessions(ReservationCommand.GetSessions command) {
        return sessionRepository.getSessions(command.memberId(), command.status(), command.pageRequest());
    }

    @Transactional
    public void cancelReservations(List<Reservation> reservations, String message) {
        // 예약 정보 취소
        reservations.forEach(reservation -> reservation.cancel(message));
        // 취소한 예약 정보 저장
        reservationRepository.saveReservations(reservations);
        // 세션 정보 찾기
        List<Session> sessions = reservations.stream()
                .map(reservation -> reservationRepository.getSession(reservation.getReservationId()))
                .flatMap(Optional::stream)
                .toList();
        //세션 정보 취소
        sessions.forEach(session -> session.cancel(message));
        // 세션 취소 정보 저장
        reservationRepository.saveSessions(sessions);
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
    public List<Reservation> fixedReserveSessions(List<Reservation> reservations) {
        // 고정 예약 진행
        List<Reservation> savedReservations = reservationRepository.saveReservations(reservations);
        // 세션 생성
        List<Session> sessions = savedReservations.stream()
                .map(reservation -> Session.builder()
                        .reservation(reservation)
                        .status(SESSION_WAITING)
                        .build())
                .toList();
        // 세션 저장
        reservationRepository.saveSessions(sessions);

        return savedReservations;
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
                .reservation(savedReservation)
                .status(SESSION_WAITING)
                .build();

        return reservationRepository.createSession(session)
                .orElseThrow(() -> new CustomException(SESSION_CREATE_FAILED));
    }


    public List<Reservation> getFixedReservations() {
        return reservationRepository.getFixedReservations();

    }
}
