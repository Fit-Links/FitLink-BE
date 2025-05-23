package spring.fitlinkbe.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.application.reservation.criteria.ReservationCriteria;
import spring.fitlinkbe.application.reservation.criteria.ReservationResult;
import spring.fitlinkbe.domain.auth.AuthService;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.notification.command.NotificationCommand;
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

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final TrainerService trainerService;
    private final NotificationService notificationService;
    private final AuthService authService;


    public List<Reservation> getReservations(LocalDate date, SecurityUser user) {

        return reservationService.getReservations(ReservationCommand.GetReservations.of(date, user.getUserRole(),
                user.getUserId()));
    }


    public ReservationResult.ReservationDetail getReservationDetail(Long reservationId) {

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

    public List<Reservation> getWaitingMembers(LocalDateTime reservationDate,
                                               SecurityUser user) {
        // 예약 대기 조회
        return reservationService.getWaitingMembers(reservationDate, user);
    }

    @Transactional
    public Reservation setDisabledReservation(ReservationCriteria.SetDisabledTime criteria, SecurityUser user) {
        //만약 그 시간에 자기가 설정한 예약 불가능 설정이 있다면 취소하기
        if (criteria.reservationId() != null) {
            return reservationService.cancelDisabledReservation(criteria.reservationId(),
                    user.getTrainerId());
        }
        // 기존에 확정된 예약이 있는지 확인
        reservationService.checkConfirmedReservationExistOrThrow(user.getTrainerId(), criteria.date());
        // 대기중인 예약이 있으면 거절
        List<Reservation> refusedReservations = reservationService.refuseWaitingReservations(List.of(criteria.date()));
        // 거절을 했다면 -> 멤버에게 예약이 거절되었다는 알림 전송
        refuseReservations(refusedReservations);

        // 예약 불가 설정한 정보 리턴
        return reservationService.setDisabledReservation(criteria.toCommand(user.getTrainerId()));
    }

    @Transactional
    public List<Reservation> createFixedReservation(ReservationCriteria.CreateFixed criteria, SecurityUser user) {
        // 세션이 충분한지 확인
        memberService.isSessionCountEnough(user.getTrainerId(), criteria.memberId());
        // 기존에 확정된 예약이 있는지 확인
        reservationService.checkConfirmedReservationsExistOrThrow(user.getTrainerId(), criteria.reservationDates());
        // 대기중인 예약이 있으면 거절
        List<Reservation> refusedReservations = reservationService.refuseWaitingReservations(criteria.reservationDates());
        // 거절을 했다면 -> 멤버에게 예약이 거절되었다는 알림 전송
        refuseReservations(refusedReservations);
        // 고정 예약 진행
        List<Reservation> reservationDomains = criteria.toDomain(memberService.getSessionInfo(user.getTrainerId(),
                criteria.memberId()), user);
        SessionInfo sessionInfo = memberService.getSessionInfo(user.getTrainerId(), criteria.memberId());
        // 세션 차감
        memberService.deductSession(user.getTrainerId(), criteria.memberId(), sessionInfo.getRemainingCount());
        List<Reservation> fixedReservations = reservationService.createFixedReservations(reservationDomains,
                sessionInfo.getRemainingCount());
        // 트레이너 -> 멤버에게 예약 됐다는 알림 전송
        reservationDomains.forEach(r -> {
            PersonalDetail memberDetail = memberService.getMemberDetail(r.getMember().getMemberId());
            Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
            notificationService.sendNotification(NotificationCommand.ApproveReservation.of(memberDetail,
                    r.getReservationId(), r.getConfirmDate(), r.getTrainer().getTrainerId(), true,
                    token.getPushToken()));
        });
        return fixedReservations;
    }

    @Transactional
    public void checkCreateFixedReservation() {
        // 고정 예약 상태의 예약 조회
        reservationService.publishFixedReservations();
    }

    @Transactional
    public void executeCreateFixedReservation(ReservationCriteria.EventCreateFixed criteria) {
        Reservation nextFixedReservation = criteria.toDomain();
        // 세션이 충분한지 확인
        memberService.isSessionCountEnough(nextFixedReservation.getTrainer().getTrainerId(),
                nextFixedReservation.getMember().getMemberId());
        // 기존에 확정된 예약이 있는지 확인
        reservationService.checkConfirmedReservationsExistOrThrow(nextFixedReservation.getTrainer().getTrainerId(),
                nextFixedReservation.getReservationDates());
        // 예약 대기중인 예약 확인
        List<Reservation> refusedReservations =
                reservationService.refuseWaitingReservations(nextFixedReservation.getReservationDates());
        // 예약 거절된 예약은 -> 멤버에게 예약 거절됐다는 메세지 전송
        refuseReservations(refusedReservations);
        // 고정 예약 진행
        reservationService.createFixedReservations(List.of(nextFixedReservation), 1);
    }

    @Transactional
    public Reservation createReservation(ReservationCriteria.Create criteria, SecurityUser user) {
        // 세션이 충분한지 확인
        memberService.isSessionCountEnough(criteria.trainerId(), criteria.memberId());
        // 새로운 예약 진행
        Reservation reservation = criteria.toDomain(memberService.getSessionInfo(criteria.trainerId(),
                criteria.memberId()), user);
        Reservation savedReservation = reservationService.createReservation(reservation);
        if (user.getUserRole() == TRAINER) {
            //만약 트레이너가 예약을 했다면, 바로 세션 생성
            reservationService.saveSession(savedReservation);
            // 세션 1회 차감
            memberService.deductSession(reservation.getTrainer().getTrainerId(), reservation.getMember().getMemberId(), 1);
            // 트레이너가 예약했다면 멤버에게 예약이 됐다는 알림 전송
            PersonalDetail memberDetail = memberService.getMemberDetail(reservation.getMember().getMemberId());
            Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
            notificationService.sendNotification(NotificationCommand.ApproveReservation.of(memberDetail,
                    savedReservation.getReservationId(), savedReservation.getConfirmDate(),
                    savedReservation.getTrainer().getTrainerId(), true, token.getPushToken()));
        }

        if (user.getUserRole() == MEMBER) {
            // 멤버가 예약했다면 트레이너에게 예약 요청을 했다는 알림 전송
            PersonalDetail trainerDetail = trainerService.getTrainerDetail(reservation.getTrainer().getTrainerId());
            Token token = authService.getTokenByPersonalDetailId(trainerDetail.getPersonalDetailId());
            notificationService.sendNotification(NotificationCommand.RequestReservation.of(trainerDetail,
                    savedReservation.getReservationId(), savedReservation.getReservationDate(),
                    savedReservation.getMember().getMemberId(), savedReservation.getName(), token.getPushToken()));
        }
        return savedReservation;
    }

    @Transactional
    public Reservation approveReservation(ReservationCriteria.Approve criteria, SecurityUser user) {
        Reservation approveReservation = reservationService.approveReservation(criteria.toApproveCommand());

        //예약 완료 알림 발송 트레이너 -> 멤버에게 예약 완료되었다는 알림 발송
        PersonalDetail memberDetail = memberService.getMemberDetail(approveReservation.getMember().getMemberId());
        Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
        notificationService.sendNotification(NotificationCommand.ApproveReservation.of(memberDetail,
                approveReservation.getReservationId(), approveReservation.getConfirmDate(),
                approveReservation.getTrainer().getTrainerId(), true, token.getPushToken()));

        List<Reservation> refuseReservations = reservationService.refuseReservations(
                criteria.toRefuseReservationsCommand(), user);

        //예약 거절 알림 발송 트레이너 -> 멤버에게 예약 거절되었다는 알림 발송
        refuseReservations(refuseReservations);

        return approveReservation;
    }

    @Transactional
    public Reservation cancelReservation(ReservationCriteria.Cancel criteria, SecurityUser user) {
        //예약 정보를 취소 한다.
        Reservation reservation = reservationService.cancelReservation(criteria.toCommand(), user);
        //트레이너의 경우
        if (user.getUserRole() == TRAINER) {
            // 세션을 하나 복구한다.
            memberService.restoreSession(reservation.getTrainer().getTrainerId(),
                    reservation.getMember().getMemberId(), 1);
            // 트레이너 -> 멤버 예약이 취소됐다는 알림 전송
            PersonalDetail memberDetail = memberService.getMemberDetail(reservation.getMember().getMemberId());
            Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
            notificationService.sendNotification(NotificationCommand.Cancel.of(
                    memberDetail, reservation.getReservationId(), reservation.getTrainer().getTrainerId(),
                    RESERVATION_CANCEL, token.getPushToken()));

            return reservation;
        }
        // 멤버의 경우
        // 멤버 -> 트레이너에게 예약 취소 요청 알림을 보낸다.
        PersonalDetail trainerDetail = trainerService.getTrainerDetail(reservation.getTrainer().getTrainerId());
        Token token = authService.getTokenByPersonalDetailId(trainerDetail.getPersonalDetailId());
        notificationService.sendNotification(NotificationCommand.CancelRequestReservation.of(trainerDetail, reservation.getReservationId(),
                reservation.getMember().getMemberId(), reservation.getName(), criteria.cancelDate(),
                criteria.cancelReason(), RESERVATION_CANCEL, token.getPushToken()));

        return reservation;
    }

    @Transactional
    public Reservation cancelApproveReservation(ReservationCriteria.CancelApproval criteria,
                                                SecurityUser user) {
        // 예약 취소 승인 여부 반영
        Reservation approvedReservation = reservationService.cancelApproveReservation(criteria.toCommand());

        //만약 예약 취소 승인이 됐다면 세션 1회 복구
        if (criteria.isApprove()) {
            memberService.restoreSession(user.getTrainerId(), criteria.memberId(), 1);
        }

        // 트레이너 -> 멤버에게 예약 취소 여부 결과 알림 발송
        PersonalDetail memberDetail = memberService.getMemberDetail(approvedReservation.getMember().getMemberId());
        Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
        notificationService.sendNotification(NotificationCommand.CancelApproveReservation.of(memberDetail, approvedReservation.getReservationId(),
                approvedReservation.getTrainer().getTrainerId(), criteria.isApprove(), token.getPushToken()));

        return approvedReservation;
    }


    @Transactional
    public Reservation changeFixedReservation(ReservationCriteria.ChangeReqeust criteria, SecurityUser user) {

        Reservation changedFixedReservation = reservationService.changeFixedReservation(criteria.toCommand());
        PersonalDetail memberDetail = memberService.getMemberDetail(changedFixedReservation.getMember().getMemberId());
        Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
        // 알림 전송 트레이너 -> 멤버에게 예약 확정 됐다는 알림 발송
        notificationService.sendNotification(NotificationCommand.ApproveReservation.of(memberDetail,
                changedFixedReservation.getReservationId(), criteria.changeRequestDate(),
                user.getTrainerId(), true, token.getPushToken()));

        return reservationService.getReservation(criteria.reservationId());
    }

    @Transactional
    public Reservation changeRequestReservation(ReservationCriteria.ChangeReqeust criteria) {

        // 알림 전송 멤버 -> 트레이너에게 예약 변경 요청했다는 알림 발송
        Reservation reservation = reservationService.changeRequestReservation(criteria.toCommand());
        PersonalDetail trainerDetail = trainerService.getTrainerDetail(reservation.getTrainer().getTrainerId());
        Token token = authService.getTokenByPersonalDetailId(trainerDetail.getPersonalDetailId());

        notificationService.sendNotification(NotificationCommand.ChangeRequestReservation.of(trainerDetail,
                reservation.getReservationId(), reservation.getMember().getMemberId(),
                reservation.getName(), criteria.reservationDate(), criteria.changeRequestDate(),
                token.getPushToken()));


        return reservation;
    }

    @Transactional
    public Reservation changeApproveReservation(ReservationCriteria.ChangeApproval criteria) {
        // 예약 변경이 승인이면 다른 예약 대기들 거절
        if (criteria.isApprove()) {
            // 대기중인 예약이 있으면 거절
            List<Reservation> refusedReservations = reservationService.refuseWaitingReservations(
                    List.of(criteria.approveDate()));
            // 거절을 했다면 -> 멤버에게 예약이 거절되었다는 알림 전송
            refuseReservations(refusedReservations);
        }
        // 예약 변경 요청 승인
        Reservation approvedReservation = reservationService.changeApproveReservation(criteria.toCommand());

        // 트레이너 -> 멤버에게 예약 변경 승인 됐다는 알림 전송
        PersonalDetail memberDetail = memberService.getMemberDetail(approvedReservation.getMember().getMemberId());
        Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
        notificationService.sendNotification(NotificationCommand.ApproveRequestReservation.of(memberDetail,
                approvedReservation.getReservationId(), approvedReservation.getTrainer().getTrainerId(),
                criteria.isApprove(), token.getPushToken()));

        return approvedReservation;
    }

    @Transactional
    public Session completeSession(ReservationCriteria.Complete criteria, SecurityUser user) {
        // 세션 처리
        Session completedSession = reservationService.completeSession(criteria.toCompleteCommand(),
                user);
        SessionInfo sessionInfo = memberService.getSessionInfo(user.getTrainerId(), criteria.memberId());
        PersonalDetail trainerDetail = trainerService.getTrainerDetail(user.getTrainerId());
        PersonalDetail memberDetail = memberService.getMemberDetail(criteria.memberId());
        Token trainerToken = authService.getTokenByPersonalDetailId(trainerDetail.getPersonalDetailId());
        Token memberToken = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
        // 알림 전송 멤버 -> 트레이너에게 멤버의 세션이 완료되었다는 알림 발송
        notificationService.sendNotification(NotificationCommand.CompleteSession.of(trainerDetail,
                completedSession.getSessionId(), user.getTrainerId(), memberDetail.getName(),
                trainerToken.getPushToken()));
        // 알림 전송 트레이너 -> 멤버에게 세션이 완료되서 차감 되었다는 알림 발송
        notificationService.sendNotification(NotificationCommand.DeductSession.of(memberDetail,
                completedSession.getSessionId(), user.getTrainerId(), memberToken.getPushToken()));
        // 만약 남은 세션 횟수가 5회면 세션 5회 남았다는 알림 전송
        if (sessionInfo.getRemainingCount() == 5) {
            notificationService.sendNotification(NotificationCommand.SessionChargeReminder.of(memberDetail,
                    sessionInfo.getSessionInfoId(), user.getTrainerId(), memberToken.getPushToken()));
        }

        return completedSession;
    }

    @Transactional
    public void checkTodaySessionReminder() {
        List<Reservation> todayReservations = reservationService.getTodayReservations();

        // 알림 전송 트레이너 -> 멤버에게 오늘 세션있다고 알림 전송
        todayReservations.forEach(r -> {
            PersonalDetail memberDetail = memberService.getMemberDetail(r.getMember().getMemberId());
            Session session = reservationService.getSession(r.getStatus(), r.getReservationId());
            Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
            notificationService.sendNotification(NotificationCommand.SessionTodayReminder.of(memberDetail,
                    session.getSessionId(), r.getTrainer().getTrainerId(), r.getConfirmDate(),
                    token.getPushToken()));
        });
    }

    @Transactional
    public void refuseReservations(List<Reservation> refusedReservations) {
        refusedReservations.forEach((r) -> {
            PersonalDetail memberDetail = memberService.getMemberDetail(r.getMember().getMemberId());
            Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
            notificationService.sendNotification(NotificationCommand.ApproveReservation.of(memberDetail, r.getReservationId(),
                    r.getReservationDate(), r.getTrainer().getTrainerId(), false, token.getPushToken()));
        });
    }

    @Transactional
    public List<Reservation> releaseFixedReservation(Long reservationId) {
        // 관련 고정 예약 모두 해지
        List<Reservation> reservations = reservationService.releaseFixedReservation(reservationId);
        Reservation reservation = reservations.get(0);
        int restoreCount = reservations.size();
        // 세션 복구
        memberService.restoreSession(reservation.getTrainer().getTrainerId(), reservation.getMember().getMemberId(),
                restoreCount);

        return reservations;
    }

}
