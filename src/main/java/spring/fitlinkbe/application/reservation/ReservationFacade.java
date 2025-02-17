package spring.fitlinkbe.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.application.reservation.criteria.ReservationCriteria;
import spring.fitlinkbe.application.reservation.criteria.ReservationResult;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationService;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final TrainerService trainerService;

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
    public Reservation setDisabledReservation(ReservationCriteria.SetDisabledTime criteria) {
        String cancelMessage = "예약 불가 설정";
        //1.  현재 시간보다 뒤에 있는 모든 예약 조회
        List<Reservation> reservations = reservationService.getReservations();
        //2. 예약 불가능한 시간대에 예약이 있나 확인
        List<Reservation> duplicatedReservations = reservations
                .stream()
                .map(reservation -> reservation.checkStatus() ? reservation : null)
                .toList();
        //2-1. 만약 그러한 예약들이 있다면 모두 취소 시킴
        if (!duplicatedReservations.isEmpty()) {
            reservationService.cancelReservations(duplicatedReservations, cancelMessage);
            //2-2. 예약 취소한 멤버들에게 예약 취소됐다는 메시지 푸쉬
            //TODO
        }
        //3. 트레이너 정보 조회
        Trainer trainerInfo = trainerService.getTrainerInfo(criteria.trainerId());
        //4. 예약 불가능한 날짜 설정 및 결과 리턴
        return reservationService.setDisabledTime(criteria.toCommand(), trainerInfo);
    }
}
