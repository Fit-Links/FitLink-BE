package spring.fitlinkbe.domain.reservation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
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

    /**
     * Related Reservation
     */

    public List<Reservation> getReservations(ReservationCommand.GetReservations command) {

        LocalDateTime startDate = command.date().atStartOfDay();
        LocalDateTime endDate = getEndDate(startDate, command.role());

        List<Reservation> reservations = reservationRepository.getReservations(command.role(), command.userId());

        return reservations.stream()
                .filter(reservation -> reservation.isReservationInRange(startDate, endDate))
                .toList();
    }

    public Reservation getReservation(Long reservationId) {
        return reservationRepository.getReservation(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservationId)));
    }

    public List<Reservation> getWaitingMembers(LocalDateTime reservationDate, SecurityUser user) {
        List<Reservation> waitingMembers = reservationRepository.getReservationsWithWaitingStatus(user.getTrainerId());

        if (waitingMembers.isEmpty()) {
            throw new CustomException(RESERVATION_WAITING_MEMBERS_EMPTY);
        }

        return waitingMembers.stream()
                .filter((r) -> r.isReservationDateSame(List.of(reservationDate)))
                .toList();
    }

    @Transactional
    public Reservation setDisabledReservation(ReservationCommand.SetDisabledTime command) {

        Reservation reservation = Reservation.builder()
                .trainer(Trainer.builder().trainerId(command.trainerId()).build())
                .reservationDates(List.of(command.date()))
                .status(DISABLED_TIME_RESERVATION)
                .build();

        // 예약 불가 설정 정보 리턴
        return reservationRepository.saveReservation(reservation).orElseThrow(() ->
                new CustomException(ErrorCode.SET_DISABLE_DATE_FAILED,
                        "예약 불가 설정을 할 수 없습니다."));
    }

    @Transactional
    public Reservation cancelDisabledReservation(Long reservationId, Long trainerId) {

        Reservation reservation = reservationRepository.getReservation(reservationId, trainerId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservationId)));

        reservation.checkDisableStatus();
        reservationRepository.deleteReservation(reservation);

        return reservation;
    }

    @Transactional
    public List<Reservation> createFixedReservation(List<Reservation> reservations) {
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
    public List<Reservation> scheduledFixedReservations() {
        // 고정 예약 상태의 예약 조회
        List<Reservation> fixedReservations = reservationRepository.getFixedReservations();
        // 일주일 뒤에 시간으로 예약 도메인 생성
        List<Reservation> newReservations = fixedReservations.stream()
                .map(Reservation::toFixedDomain)
                .toList();
        // 일주일 뒤에 시간에 예약이 있다면(예약 대기 포함) 취소 절차 진행
        newReservations.forEach((r) -> cancelExistReservations(r.getReservationDates(), "트레이너 고정 예약"
                , null));

        return newReservations;
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        return reservationRepository.saveReservation(reservation)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.RESERVATION_IS_FAILED,
                                "예약에 실패하였습니다."));
    }

    @Transactional
    public Reservation approveReservation(ReservationCommand.Approve command) {

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

        List<Reservation> reservations = reservationRepository.getReservations(user.getUserRole(), user.getUserId());

        List<Reservation> refuseReservations = reservations.stream()
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
    public Reservation cancelReservation(ReservationCommand.Cancel command, SecurityUser user) {
        Reservation reservation = this.getReservation(command.reservationId());
        //트레이너의 경우
        if (user.getUserRole() == TRAINER) {
            // 예약을 취소한다.
            reservation.cancelRequest("트레이너가 예약을 취소하였습니다", command.cancelDate(), user.getUserRole());
            Reservation canceledReservation = reservationRepository.saveReservation(reservation)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_CANCEL_FAILED,
                            "예약 취소를 실패하였습니다. [reservationId: %d]".formatted(command.reservationId())));
            // 세션이 있는 경우, 세션도 취소한다.
            Session getSession = reservationRepository.getSession(reservation.getReservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                            "세션 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservation.getReservationId())));
            getSession.cancel("트레이너의 요청으로 세션이 최소되었습니다");
            reservationRepository.saveSession(getSession);

            return canceledReservation;
        }
        // 멤버의 경우
        // 예약 취소 요청
        reservation.cancelRequest("회원이 예약 취소를 요청 하였습니다.", command.cancelDate(), user.getUserRole());
        // 취소한 예약 정보 저장
        return reservationRepository.saveReservation(reservation).orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_CANCEL_FAILED,
                "예약 취소 요청에 실패하였습니다. [reservationId: %d]".formatted(command.reservationId())));
    }

    @Transactional
    public List<Reservation> cancelExistReservations(List<LocalDateTime> reservationDates,
                                                     String cancelReason, Long reservationId) {
        //1. 일치하는 날짜의 예약 조회
        List<Reservation> reservations = reservationRepository.getReservations()
                .stream()
                .filter(Reservation::isAlreadyCancel)
                .filter(r -> !r.getReservationId().equals(reservationId))
                .filter(r -> r.isReservationDateSame(reservationDates))
                .toList();

        //2. 이미 존재하는 예약 취소 절차 진행
        if (!reservations.isEmpty()) {
            reservations.forEach(Reservation::checkPossibleReserveStatus);
            cancelReservations(reservations, cancelReason);

            return reservations;
        }

        return List.of();
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
    public Reservation cancelApproveReservation(ReservationCommand.CancelApproval command) {
        Reservation reservation = getReservation(command.reservationId());
        reservation.approveCancelReqeust(command.memberId(), command.isApprove());

        Optional<Session> session = reservationRepository.getSession(command.reservationId());

        //만약 세션이 있다면 세션도 취소
        if (command.isApprove() && session.isPresent()) {
            session.get().cancel("예약 취소 요청으로 인한 취소");
            reservationRepository.saveSession(session.get());

        }

        return reservationRepository.saveReservation(reservation)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_IS_FAILED,
                        "예약 취소 승인에 실패하였습니다."));
    }

    @Transactional
    public Reservation changeReservation(ReservationCommand.ChangeReqeust command,
                                         SecurityUser user) {
        Reservation reservation = this.getReservation(command.reservationId());
        // 변경하고자 하는 날짜에 확정된 예약이 있는지 확인
        this.checkConfirmedReservationExistOrThrow(reservation.getTrainer().getTrainerId(),
                command.changeRequestDate());
        // 트레이너의 경우 고정 예약 변경
        if (user.getUserRole() == TRAINER) {
            reservation.changeFixedDate(command.reservationDate(), command.changeRequestDate());

            return reservationRepository.saveReservation(reservation)
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_IS_FAILED,
                            "예약 요청 변경에 실패하였습니다."));
        }
        // 멤버의 경우 예약 변경 요청
        reservation.changeRequestDate(command.reservationDate(), command.changeRequestDate());

        return reservationRepository.saveReservation(reservation)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_IS_FAILED,
                        "예약 요청 변경에 실패하였습니다."));
    }

    @Transactional
    public Reservation changeApproveReservation(ReservationCommand.ChangeApproval command) {

        Reservation reservation = this.getReservation(command.reservationId());
        reservation.approveChangeReqeust(command.memberId(), command.isApprove());

        //만약 만들어진 세션이 없다면 생성
        Optional<Session> session = reservationRepository.getSession(reservation.getReservationId());

        if (session.isEmpty()) {
            Session sessionDomain = Session.createSession(reservation.getReservationId());
            reservationRepository.saveSession(sessionDomain);
        }

        return reservationRepository.saveReservation(reservation)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_IS_FAILED,
                        "예약 변경 승인에 실패하였습니다."));

    }

    @Transactional
    public Session completeSession(ReservationCommand.Complete command, SecurityUser user) {

        Session session = reservationRepository.getSession(command.reservationId()).orElseThrow(() ->
                new CustomException(ErrorCode.SESSION_NOT_FOUND,
                        "세션 정보를 찾지 못하였습니다. [reservationId: %d].".formatted(command.reservationId())));
        // 세션 완료 처리
        session.complete(command.isJoin());

        Reservation reservation = reservationRepository.getReservation(command.reservationId()).orElseThrow(() ->
                new CustomException(ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보를 찾지 못하였습니다. [reservationId: %d].".formatted(command.reservationId())));

        // 예약 종료 처리
        reservation.complete(user.getTrainerId(), command.memberId());

        reservationRepository.saveReservation(reservation).orElseThrow(() ->
                new CustomException(ErrorCode.RESERVATION_IS_FAILED));

        return reservationRepository.saveSession(session).orElseThrow(() ->
                new CustomException(ErrorCode.SESSION_CREATE_FAILED));

    }

    /**
     * Related Session
     */

    public Page<Session> getSessions(ReservationCommand.GetSessions command) {
        return sessionRepository.getSessions(command.memberId(), command.trainerId(),
                command.status(), command.pageRequest());
    }

    public Session getSession(Status status, Long reservationId) {
        Optional<Session> session = reservationRepository.getSession(reservationId);
        // 예약 승낙 전에는 세션 정보는 없다.
        if (status == RESERVATION_WAITING) {
            return null;
        }

        return session.orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                "세션 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservationId)));
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


    /**
     * 해당 날짜에 확정된 예약이 있는지 검사
     */
    public void checkConfirmedReservationExistOrThrow(Long trainerId, List<LocalDate> dates) {
        if (reservationRepository.isConfirmedReservationExists(trainerId, dates)) {
            throw new CustomException(ErrorCode.CONFIRMED_RESERVATION_EXISTS);
        }
    }

    /**
     * 해당 날짜와 시간에 확정된 예약이 있는지 검사
     */
    public void checkConfirmedReservationExistOrThrow(Long trainerId, LocalDateTime checkDate) {
        if (reservationRepository.isConfirmedReservationExists(trainerId, checkDate)) {
            throw new CustomException(ErrorCode.CONFIRMED_RESERVATION_EXISTS);
        }
    }
}
