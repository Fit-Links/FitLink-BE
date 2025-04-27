package spring.fitlinkbe.domain.reservation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.reservation.event.GenerateFixedReservationEvent;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.infra.producer.EventTopic;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final ApplicationEventPublisher publisher;
    private final EventTopic eventTopic;

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
    public List<Reservation> createFixedReservations(List<Reservation> baseReservations, int remainingCount) {


        List<Reservation> newFixedReservations = Reservation.createFixedReservations(baseReservations, remainingCount);

        List<Reservation> savedReservations = reservationRepository.saveReservations(newFixedReservations);

        // 세션 생성
        List<Session> sessions = savedReservations.stream()
                .map(reservation -> Session.builder()
                        .reservation(reservation)
                        .status(SESSION_WAITING)
                        .build())
                .toList();

        reservationRepository.saveSessions(sessions);

        return savedReservations;
    }

    public void publishFixedReservations() {
        // 고정 예약 상태의 예약 조회
        List<Reservation> fixedReservations = reservationRepository.getFixedReservations();
        // 오늘 날짜 고정 예약들 필터하기
        List<Reservation> todayFixedReservations = fixedReservations.stream()
                .filter(Reservation::isTodayReservation)
                .toList();
        // 고정 예약건 개별 이벤트 발행
        todayFixedReservations.forEach(reservation ->
                publisher.publishEvent(GenerateFixedReservationEvent.builder()
                        .reservationId(reservation.getReservationId())
                        .trainerId(reservation.getTrainer().getTrainerId())
                        .memberId(reservation.getMember().getMemberId())
                        .sessionInfoId(reservation.getSessionInfo().getSessionInfoId())
                        .name(reservation.getName())
                        .confirmDate(reservation.getConfirmDate())
                        .topic(eventTopic.getReservationQueue())
                        .messageId(UUID.randomUUID().toString())
                        .build()));
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
    public List<Reservation> refuseWaitingReservations(List<LocalDateTime> dates) {
        //1. 일치하는 날짜의 대기 예약 조회
        List<Reservation> reservations = reservationRepository.getReservations()
                .stream()
                .filter(r -> r.getStatus() == RESERVATION_WAITING)
                .filter(r -> r.isReservationDateSame(dates))
                .toList();

        //2. 대기 중인 예약 거절
        reservations.forEach((r) -> {
            r.refuse();
            reservationRepository.saveReservation(r);
        });

        if (!reservations.isEmpty()) return reservations;

        return List.of();
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
    public List<Reservation> changeFixedReservation(ReservationCommand.ChangeReqeust command, SecurityUser user) {
        Reservation reservation = this.getReservation(command.reservationId());
        // 변경하고자 하는 날짜에 확정된 예약이 있는지 확인
        this.checkConfirmedReservationExistOrThrow(reservation.getTrainer().getTrainerId(), command.changeRequestDate());
        // 이전에 예약한 모든 고정예약 취소
        List<Reservation> cancelBeforeFixedReservations = cancelAllBeforeFixedReservation(user.getTrainerId(),
                reservation.getMember().getMemberId(), command.reservationId(), command.reservationDate());
        reservation.changeFixedDate(command.reservationDate(), command.changeRequestDate());

        Reservation savedReservation = reservationRepository.saveReservation(reservation)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_IS_FAILED, "고정 예약 변경에 실패하였습니다."));

        return cancelBeforeFixedReservations.isEmpty() ? List.of(savedReservation) : cancelBeforeFixedReservations;
    }

    @Transactional
    public Reservation changeRequestReservation(ReservationCommand.ChangeReqeust command) {
        Reservation reservation = this.getReservation(command.reservationId());
        // 변경하고자 하는 날짜에 확정된 예약이 있는지 확인
        this.checkConfirmedReservationExistOrThrow(reservation.getTrainer().getTrainerId(), command.changeRequestDate());

        // 멤버의 경우 예약 변경 요청
        reservation.changeRequestDate(command.reservationDate(), command.changeRequestDate());

        return reservationRepository.saveReservation(reservation)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_IS_FAILED, "예약 요청 변경에 실패하였습니다."));
    }

    @Transactional
    public List<Reservation> cancelAllBeforeFixedReservation(Long trainerId, Long memberId,
                                                             Long reservationId, LocalDateTime fixedReservationDate) {
        List<Reservation> fixedReservations = reservationRepository.getFixedReservations(trainerId, fixedReservationDate)
                .stream().filter(r -> r.isSameReservationAndMember(reservationId, memberId)).toList();

        // 세션이 있는 경우, 세션도 취소한다.
        List<Session> sessions = new ArrayList<>();
        fixedReservations.forEach(fixedReservation -> {
            fixedReservation.cancel("고정 예약 변경으로 취소되었습니다.");
            Session getSession = reservationRepository.getSession(fixedReservation.getReservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                            "세션 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(fixedReservation.getReservationId())));
            getSession.cancel("트레이너의 요청으로 세션이 최소되었습니다");
            sessions.add(getSession);
        });
        reservationRepository.saveSessions(sessions);

        return reservationRepository.saveReservations(fixedReservations);
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

    @Transactional
    public List<Reservation> releaseFixedReservation(Long reservationId) {
        Reservation reservation = this.getReservation(reservationId);
        List<Reservation> fixedReservations = reservationRepository.getFixedReservations(reservation.getMember().getMemberId());

        List<Reservation> releaseFixedReservations = fixedReservations.stream()
                .filter(r -> r.isFixedWithBaseDate(reservation.getConfirmDate()))
                .map(r -> r.cancel("예약 해지로 취소되었습니다."))
                .toList();

        // 세션이 있는 경우, 세션도 취소한다.
        List<Session> sessions = new ArrayList<>();
        releaseFixedReservations.forEach(r -> {
            Session session = reservationRepository.getSession(r.getReservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                            "세션 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(r.getReservationId())));
            session.cancel("트레이너의 요청으로 세션이 최소되었습니다");

            sessions.add(session);
        });
        reservationRepository.saveSessions(sessions);

        return reservationRepository.saveReservations(releaseFixedReservations);
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

    public List<Reservation> getTodayReservations() {
        LocalDate today = LocalDateTime.now().toLocalDate();
        List<Reservation> reservations = reservationRepository.getReservations();

        return reservations.stream().filter(r -> r.getConfirmDate().toLocalDate().isEqual(today))
                .toList();
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
    public void checkConfirmedReservationsExistOrThrow(Long trainerId, List<LocalDateTime> checkDates) {
        if (reservationRepository.isConfirmedReservationsExists(trainerId, checkDates)) {
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
