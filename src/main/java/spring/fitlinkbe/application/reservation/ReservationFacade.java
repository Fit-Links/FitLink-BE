package spring.fitlinkbe.application.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationService;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final MemberService memberService;

    public ReservationResult.Reservations getReservations(LocalDate date, SecurityUser user) {

        return ReservationResult.Reservations.from(reservationService
                .getReservations(date, user.getUserRole(), user.getUserId()));
    }


    @Transactional(readOnly = true)
    public ReservationResult.ReservationDetail getReservation(Long reservationId) {
        //예약 상세 정보 조회
        Reservation reservation = reservationService.getReservation(reservationId);

        //세션 정보 조회
        Session session = reservationService.getSession(reservation.isApproved(),
                reservationId);

        // 개인 정보 조회
        PersonalDetail personalDetail = memberService.getMemberDetail(reservation.getMember().getMemberId());

        // 조합해서 리턴
        return ReservationResult.ReservationDetail.from(reservation, session, personalDetail);
    }
}
