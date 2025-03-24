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
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static spring.fitlinkbe.domain.common.enums.UserRole.TRAINER;
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
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SessionRepository sessionRepository;

    public List<Reservation> getReservations() {

        return reservationRepository.getReservations()
                .stream().filter(Reservation::isAlreadyCancel)
                .toList();
    }

    public List<Reservation> getReservationThatTimes(ReservationCommand.GetReservationThatTimes
                                                             command) {
        List<Reservation> reservations = reservationRepository.getReservations(UserRole.TRAINER, command.trainerId());

        // 해당 시간의 예약만 리턴
        return reservations.stream()
                .filter((reservation) -> reservation.isReservationDateSame(command.date()))
                .filter(Reservation::isAlreadyCancel) // 이미 취소된 예약인지 확인
                .toList();
    }


    public List<Reservation> getReservations(ReservationCommand.GetReservations command) {

        LocalDateTime startDate = command.date().atStartOfDay();
        LocalDateTime endDate = getEndDate(startDate, command.role());

        List<Reservation> reservations = reservationRepository.getReservations(command.role(), command.userId());

        return reservations.stream()
                .filter(reservation -> reservation.isReservationInRange(startDate, endDate))
                .toList();
    }

    public List<Reservation> getReservationsWithWaitingStatus(Long trainerId) {
        List<Reservation> WaitingMembers = reservationRepository.getReservationsWithWaitingStatus(trainerId);

        if (WaitingMembers.isEmpty()) {
            throw new CustomException(RESERVATION_WAITING_MEMBERS_EMPTY);
        }

        return WaitingMembers;
    }

    public Reservation getReservation(Long reservationId) {
        return reservationRepository.getReservation(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservationId)));
    }

    public Session getSession(Status status, Long reservationId) {
        Optional<Session> getSession = reservationRepository.getSession(reservationId);
        // 예약 승낙 전에는 세션 정보는 없다.
        if (status == RESERVATION_WAITING) {
            return null;
        }

        return getSession.orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                "세션 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservationId)));
    }

    public Page<Session> getSessions(ReservationCommand.GetSessions command) {
        return sessionRepository.getSessions(command.memberId(), command.trainerId(),
                command.status(), command.pageRequest());
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
    public Reservation approveReservation(ReservationCommand.ApproveReservation command) {

        Reservation reservation = this.getReservation(command.reservationId());
        reservation.approve(command.reservationDate());

        Reservation savedReservation = reservationRepository.saveReservation(reservation)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_IS_FAILED,
                        "예약 승인에 실패하였습니다."));

        // 세션 생성
        Session session = Session.builder()
                .reservation(reservation)
                .status(SESSION_WAITING)
                .build();
        // 세션 저장
        reservationRepository.saveSession(session);

        return savedReservation;
    }

    @Transactional
    public List<Reservation> refuseReservations(ReservationCommand.RefuseReservations command, SecurityUser user) {

        List<Reservation> getReservations = reservationRepository.getReservations(user.getUserRole(), user.getUserId());

        List<Reservation> refuseReservations = getReservations.stream()
                .filter(r -> r.getStatus() == RESERVATION_WAITING)
                .filter(r -> r.isReservationDateSame(List.of(command.reservationDate())))
                .toList();

        refuseReservations.forEach(Reservation::refuse);

        return reservationRepository.saveReservations(refuseReservations);

    }

    /**
     * 예약 취소
     * 트레이너의 경우, 바로 예약을 취소할 수 있다. 단, 취소 후 멤버에게 예약을 취소했다는 알림을 보내야 한다.
     * (*) 트레이너의 경우, 바로 예약을 취소할 수 있으며, 회원의 세션을 하나 복구해줘야 한다.
     * 멤버의 경우, 예약을 바로 취소할 수는 없고, 예약 취소를 요청을 한다. 그 후, 트레이너에게 예약 취소 요청 알림을 보낸다.
     * (*) 멤버의 경우, 예약 취소는 당일에는 불가하며, 해당 날짜의 전날 오후 11시까지 가능하다.
     */
    @Transactional
    public Reservation cancelReservation(ReservationCommand.CancelReservation command, SecurityUser user) {
        Reservation reservation = this.getReservation(command.reservationId());
        //트레이너의 경우
        if (user.getUserRole() == TRAINER) {
            // 예약을 취소한다.
            reservation.cancel("트레이너가 예약을 취소하였습니다");
            Reservation cancelReservation = reservationRepository.saveReservation(reservation)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_CANCEL_FAILED,
                            "예약 취소를 실패하였습니다. [reservationId: %d]".formatted(command.reservationId())));
            // 세션이 있는 경우, 세션도 취소한다.
            Session getSession = reservationRepository.getSession(reservation.getReservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                            "세션 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservation.getReservationId())));
            getSession.cancel("트레이너의 요청으로 세션이 최소되었습니다");
            reservationRepository.saveSession(getSession);

            return cancelReservation;
        }
        // 멤버의 경우
        // 예약 취소 요청
        reservation.cancelRequest("회원이 예약 취소를 요청 하였습니다.");
        // 취소한 예약 정보 저장
        return reservationRepository.saveReservation(reservation).orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_CANCEL_FAILED,
                "예약 취소 요청에 실패하였습니다. [reservationId: %d]".formatted(command.reservationId())));
    }

    @Transactional
    public Reservation setDisabledTime(ReservationCommand.SetDisabledTime command, Trainer trainerInfo) {

        Reservation reservation = Reservation.builder()
                .trainer(trainerInfo)
                .reservationDates(List.of(command.date()))
                .status(DISABLED_TIME_RESERVATION)
                .build();

        return reservationRepository.saveReservation(reservation).orElseThrow(() ->
                new CustomException(ErrorCode.SET_DISABLE_DATE_FAILED,
                        "예약 불가 설정을 할 수 없습니다."));
    }

    @Transactional
    public List<Reservation> fixedReserveSession(List<Reservation> reservations) {
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
        return reservationRepository.saveReservation(reservation)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.RESERVATION_IS_FAILED,
                                "예약에 실패하였습니다."));
    }

    @Transactional
    public Session completeSession(ReservationCommand.CompleteSession command) {

        Session getSession = reservationRepository.getSession(command.reservationId()).orElseThrow(() ->
                new CustomException(ErrorCode.SESSION_NOT_FOUND,
                        "세션 정보를 찾지 못하였습니다. [reservationId: %d].".formatted(command.reservationId())));

        getSession.complete(command.isJoin());

        return reservationRepository.saveSession(getSession).orElseThrow(() ->
                new CustomException(ErrorCode.SESSION_CREATE_FAILED));

    }

    @Transactional
    public Reservation completeReservation(ReservationCommand.CompleteReservation command, SecurityUser user) {
        Reservation getReservation = reservationRepository.getReservation(command.reservationId()).orElseThrow(() ->
                new CustomException(ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보를 찾지 못하였습니다. [reservationId: %d].".formatted(command.reservationId())));

        getReservation.complete(user.getTrainerId(), command.memberId());

        return reservationRepository.saveReservation(getReservation).orElseThrow(() ->
                new CustomException(ErrorCode.RESERVATION_IS_FAILED));
    }

    @Transactional
    public Session saveSession(Reservation savedReservation) {

        Session session = Session.builder()
                .reservation(savedReservation)
                .status(SESSION_WAITING)
                .build();

        return reservationRepository.saveSession(session)
                .orElseThrow(() -> new CustomException(SESSION_CREATE_FAILED));
    }

    public List<Reservation> getFixedReservations() {
        return reservationRepository.getFixedReservations();

    }


}
