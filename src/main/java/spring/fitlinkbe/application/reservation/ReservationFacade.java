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
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.util.List;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;
import static spring.fitlinkbe.domain.common.enums.UserRole.TRAINER;
import static spring.fitlinkbe.domain.notification.Notification.Reason;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final TrainerService trainerService;
    private final NotificationService notificationService;


    public ReservationResult.Reservations getReservations(LocalDate date, SecurityUser user) {

        return ReservationResult.Reservations.from(reservationService
                .getReservations(date, user.getUserRole(), user.getUserId()));
    }

    @Transactional(readOnly = true)
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
        String cancelMessage = "예약 불가 설정";
        //1. 모든 예약 조회
        List<Reservation> reservations = reservationService.getReservations();
        //2. 예약 불가능한 시간대나 현시간보다 뒤에 예약이 있나 확인
        List<Reservation> duplicatedReservations = reservations.stream()
                .filter(reservation -> reservation.isReservationAfterToday(reservation))
                .map(reservation -> reservation.checkStatus() ? reservation : null)
                .toList();
        //2-1. 만약 그러한 예약들이 있다면 모두 취소 시킴
        if (!duplicatedReservations.isEmpty()) {
            reservationService.cancelReservations(duplicatedReservations, cancelMessage);
            //2-2. 예약 취소한 멤버들에게 예약 취소됐다는 메시지 푸쉬
            duplicatedReservations.forEach(reservation -> {
                PersonalDetail memberDetail = memberService.getMemberDetail(reservation.getMember().getMemberId());
                notificationService.sendCancelReservationNotification(reservation.getReservationId(), memberDetail, Reason.DAY_OFF);
            });
        }
        //3. 트레이너 정보 조회
        Trainer trainerInfo = trainerService.getTrainerInfo(user.getTrainerId());
        //4. 예약 불가능한 날짜 설정 및 결과 리턴
        return reservationService.setDisabledTime(criteria.toCommand(), trainerInfo);
    }

    @Transactional
    public Reservation reserveSession(ReservationCriteria.ReserveSession criteria
            , SecurityUser user) {
        Reservation reservation = criteria.toDomain(memberService.getSessionInfo(criteria.trainerId(),
                criteria.memberId()), user);
        Reservation savedReservation = reservationService.reserveSession(reservation);

        if (user.getUserRole() == TRAINER) {
            //만약 트레이너가 예약을 했다면, 바로 세션 생성
            reservationService.createSession(savedReservation);
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
}
