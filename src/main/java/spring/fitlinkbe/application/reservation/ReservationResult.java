package spring.fitlinkbe.application.reservation;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.Session;

import java.util.List;

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

    @Builder(toBuilder = true)
    public record Reservations(List<Reservation> reservations) {

        public static Reservations from(List<Reservation> reservations) {

            return ReservationResult.Reservations.builder()
                    .reservations(reservations)
                    .build();
        }
    }
}
