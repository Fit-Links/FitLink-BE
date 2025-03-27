package spring.fitlinkbe.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.application.reservation.criteria.ReservationCriteria;
import spring.fitlinkbe.application.reservation.criteria.ReservationResult;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationService;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.TrainerService;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;
import static spring.fitlinkbe.domain.common.enums.UserRole.TRAINER;
import static spring.fitlinkbe.domain.notification.Notification.Reason.RESERVATION_CANCEL;
import static spring.fitlinkbe.domain.notification.Notification.Reason.RESERVATION_REFUSE;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final TrainerService trainerService;
    private final NotificationService notificationService;


    public ReservationResult.Reservations getReservations(LocalDate date, SecurityUser user) {

        return ReservationResult.Reservations.from(reservationService
                .getReservations(ReservationCommand.GetReservations.of(date, user.getUserRole(), user.getUserId())));
    }


    public ReservationResult.ReservationDetail getReservation(Long reservationId) {

        //예약 상세 정보 조회
        Reservation reservation = reservationService.getReservation(reservationId);
        //세션 정보 조회
        Session session = reservationService.getSession(reservation.getStatus(),
                reservationId);
        // 개인 정보 조회
        PersonalDetail personalDetail = memberService.getMemberDetail(reservation.getMember().getMemberId());
        // 조합해서 리턴
        return ReservationResult.ReservationDetail.from(reservation, session, personalDetail);
    }

    @Transactional
    public Reservation setDisabledReservation(ReservationCriteria.SetDisabledTime criteria, SecurityUser user) {
        // 기존에 있던 예약들 취소
        List<Reservation> cancelledReservations = reservationService.cancelExistReservations(criteria.date());
        // 예약이 취소되었다면, 트레이너 -> 멤버 예약 취소됐다는 알람 전송
        cancelledReservations.forEach(r -> notificationService.sendCancelReservationNotification(r.getReservationId(),
                memberService.getMemberDetail(r.getMember().getMemberId()), RESERVATION_REFUSE));
        // 예약 불가 설정한 정보 리턴
        return reservationService.setDisabledReservation(criteria.toCommand(user.getTrainerId()));
    }

    @Transactional
    public Reservation reserveSession(ReservationCriteria.ReserveSession criteria
            , SecurityUser user) {
        Reservation reservation = criteria.toDomain(memberService.getSessionInfo(criteria.trainerId(),
                criteria.memberId()), user);
        Reservation savedReservation = reservationService.reserveSession(reservation);

        if (user.getUserRole() == TRAINER) {
            //만약 트레이너가 예약을 했다면, 바로 세션 생성
            reservationService.saveSession(savedReservation);
            // 트레이너가 예약했다면 멤버에게 예약이 됐다는 알람 전송
            PersonalDetail memberDetail = memberService.getMemberDetail(reservation.getMember().getMemberId());
            notificationService.sendApproveReservationNotification(savedReservation.getReservationId(), memberDetail);
        }

        if (user.getUserRole() == MEMBER) {
            // 멤버가 예약했다면 트레이너에게 예약 요청을 했다는 알람 전송
            PersonalDetail trainerDetail = trainerService.getTrainerDetail(reservation.getTrainer().getTrainerId());
            notificationService.sendRequestReservationNotification(savedReservation, trainerDetail);
        }
        return savedReservation;
    }

    public List<ReservationResult.ReservationWaitingMember> getWaitingMembers(LocalDateTime reservationDate, SecurityUser user) {
        // 예약 조회
        List<Reservation> reservations = reservationService.getReservationsWithWaitingStatus(user.getTrainerId());
        // 예약 날짜 일치하는거 필터
        List<Reservation> filteredList = reservations.stream()
                .filter((r) -> r.isReservationDateSame(List.of(reservationDate)))
                .filter(Reservation::isWaitingStatus) //만약 이 시간대 예약 대기 상태가 아닌게 발견되면 예외 던짐
                .toList();
        // 예약들 마다 멤버 디테일 정보 조회 및 조합해서 리턴
        return filteredList.stream()
                .map(reservation -> ReservationResult.ReservationWaitingMember.from(reservation,
                        memberService.getMemberDetail(reservation.getMember().getMemberId())))
                .toList();
    }

    @Transactional
    public ReservationResult.Reservations fixedReserveSession(ReservationCriteria.FixedReserveSession criteria,
                                                              SecurityUser user) {
        // 해당 시간에 예약이 있는지 조회
        List<Reservation> reservations = reservationService
                .getReservationThatTimes(criteria.toCommand(user));
        // 만약 예약이 있다면 취소 진행
        cancelExistingReservations(reservations, "트레이너의 고정 예약으로 인해 예약이 취소되었습니다.");
        // 고정 예약 진행
        List<Reservation> reservationDomains = criteria.toDomain(memberService.getSessionInfo(user.getTrainerId(),
                criteria.memberId()), user);
        List<Reservation> fixedReservation = reservationService.fixedReserveSession(reservationDomains);
        // 고정 예약 완료 정보 리턴
        return ReservationResult.Reservations.from(fixedReservation);
    }

    @Transactional
    public Reservation cancelReservation(ReservationCriteria.CancelReservation criteria, SecurityUser user) {

        //예약 정보를 취소 한다.
        Reservation reservation = reservationService.cancelReservation(criteria.toCommand(), user);
        //트레이너의 경우
        if (user.getUserRole() == TRAINER) {
            // 세션을 하나 복구한다.
            memberService.restoreSession(reservation.getTrainer().getTrainerId(),
                    reservation.getMember().getMemberId());
            // 트레이너 -> 멤버 예약이 취소됐다는 알람 전송
            notificationService.sendCancelReservationNotification(reservation.getReservationId(),
                    memberService.getMemberDetail(reservation.getMember().getMemberId()), RESERVATION_REFUSE);
            return reservation;
        }
        // 멤버의 경우
        // 멤버 -> 트레이너에게 예약 취소 요청 알림을 보낸다.
        notificationService.sendCancelRequestReservationNotification(reservation.getReservationId(), reservation.getName(),
                criteria.cancelDate(), criteria.cancelReason(),
                trainerService.getTrainerDetail(reservation.getTrainer().getTrainerId()), RESERVATION_CANCEL);
        return reservation;
    }

    @Transactional
    public void checkFixedReserveSession() {
        // 고정 예약 상태의 예약 조회
        List<Reservation> fixedReservations = reservationService.getFixedReservations();

        // 일주일 뒤에 시간으로 예약 도메인 생성
        List<Reservation> newReservations = fixedReservations.stream()
                .map(Reservation::toFixedDomain)
                .toList();
        // 일주일 뒤에 시간에 예약이 있다면(예약 대기 포함) 취소 절차 진행
        newReservations.forEach((r) -> {
            List<Reservation> getThatTimeReservations = reservationService.getReservationThatTimes(
                    ReservationCommand.GetReservationThatTimes.builder()
                            .trainerId(r.getTrainer().getTrainerId())
                            .date(r.getReservationDates())
                            .build());

            cancelExistingReservations(getThatTimeReservations, "트레이너의 고정 예약으로 인해 예약이 취소되었습니다.");
        });
        // 고정 예약 진행
        reservationService.fixedReserveSession(newReservations);
    }

    @Transactional
    public Reservation approveReservation(ReservationCriteria.ApproveReservation criteria, SecurityUser user) {
        Reservation approveReservation = reservationService.approveReservation(criteria.toApproveReservationCommand());

        //예약 완료 알람 발송 트레이너 -> 멤버에게 예약 완료되었다는 알람 발송
        notificationService.sendApproveReservationNotification(approveReservation.getReservationId(),
                memberService.getMemberDetail(approveReservation.getMember().getMemberId()));

        List<Reservation> refuseReservations = reservationService.refuseReservations(
                criteria.toRefuseReservationsCommand(), user);

        //예약 거절 알람 발송 트레이너 -> 멤버에게 예약 거절되었다는 알람 발송
        refuseReservations.forEach((refuseReservation) -> notificationService.sendRefuseReservationNotification(refuseReservation.getReservationId(),
                memberService.getMemberDetail(refuseReservation.getMember().getMemberId())));

        return approveReservation;
    }

    @Transactional
    public Session completeSession(ReservationCriteria.CompleteSession criteria, SecurityUser user) {
        // 세션 처리
        Session completedSession = reservationService.completeSession(criteria.toCompleteSessionCommand());
        // 세션 하나 차감
        memberService.deductSession(user.getTrainerId(), criteria.memberId());
        // 예약 완료 처리
        reservationService.completeReservation(criteria.toCompleteReservationCommand(), user);
        // 알람 전송 트레이너 -> 멤버에게 세션이 완료되서 차감 되었다는 알람 발송
        notificationService.sendCompleteSessionNotification(completedSession.getSessionId(),
                memberService.getMemberDetail(criteria.memberId()));

        return completedSession;
    }

    @Transactional
    public Reservation changeReqeustReservation(ReservationCriteria.ChangeReqeustReservation criteria) {
        // 예약 변경 요청
        Reservation requestedReservation = reservationService.changeReqeustReservation(criteria.toCommand());
        // 알람 전송 멤버 -> 트레이너에게 예약 변경 요청했다는 알람 발송
        notificationService.sendChangeRequestReservationNotification(requestedReservation.getReservationId(),
                requestedReservation.getName(),
                criteria.reservationDate(), criteria.changeRequestDate(),
                trainerService.getTrainerDetail(requestedReservation.getTrainer().getTrainerId()));

        return requestedReservation;
    }

    @Transactional
    public Reservation changeApproveReservation(ReservationCriteria.ChangeApproveReservation criteria) {
        // 예약 변경 요청 승인
        Reservation approvedReservation = reservationService.changeApproveReservation(criteria.toCommand());
        // 다른 같은 시간대 예약 모두 취소
        if (criteria.isApprove()) {
            List<Reservation> getThatTimeReservations = reservationService.getReservationThatTimes(
                    ReservationCommand.GetReservationThatTimes.builder()
                            .trainerId(approvedReservation.getTrainer().getTrainerId())
                            .date(approvedReservation.getReservationDates())
                            .build());

            List<Reservation> filteredReservations = getThatTimeReservations.stream()
                    .filter(r -> !r.getReservationId().equals(approvedReservation.getReservationId()))
                    .toList();

            cancelExistingReservations(filteredReservations, "트레이너의 의해 예약 요청이 거절되었습니다.");
        }
        // 트레이너 -> 멤버에게 예약 변경 승인 됐다는 알람 전송
        notificationService.sendApproveRequestReservationNotification(approvedReservation.getReservationId(),
                memberService.getMemberDetail(approvedReservation.getMember().getMemberId()),
                criteria.isApprove());

        return approvedReservation;
    }

    private void cancelExistingReservations(List<Reservation> reservations, String cancelMsg) {
        if (!reservations.isEmpty()) {
            reservations.forEach(Reservation::checkPossibleReserveStatus);
            // 만약 예약이 있다면, 그 예약들 모두 강제로 취소
            reservationService.cancelReservations(reservations, cancelMsg);
            // 취소했다면, 취소됐다는 알람 전송
            reservations.forEach(r -> notificationService.sendCancelReservationNotification(r.getReservationId(),
                    memberService.getMemberDetail(r.getMember().getMemberId()), RESERVATION_REFUSE));
        }
    }


}
