package spring.fitlinkbe.application.reservation.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.Session;

public class ReservationResult {

    @Builder(toBuilder = true)
    public record ReservationDetail(Reservation reservation, Session session, PersonalDetail personalDetail) {

        public static ReservationDetail from(Reservation reservation, Session session, PersonalDetail personalDetail) {

            return ReservationResult.ReservationDetail.builder()
                    .reservation(reservation)
                    .session(session)
                    .personalDetail(personalDetail)
                    .build();
        }
    }
}
