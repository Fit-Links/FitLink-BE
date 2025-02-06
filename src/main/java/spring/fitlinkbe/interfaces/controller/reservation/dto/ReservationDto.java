package spring.fitlinkbe.interfaces.controller.reservation.dto;

import lombok.Builder;
import spring.fitlinkbe.domain.reservation.Reservation;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class ReservationDto {

    @Builder(toBuilder = true)
    public record Response(Long reservationId, Long sessionInfoId,
                           boolean isDayOff, DayOfWeek dayOfWeek, LocalDateTime reservationDate,
                           Reservation.Status status, MemberInfo memberInfo) {

        public static ReservationDto.Response of(Reservation reservation) {

            return Response.builder()
                    .reservationId(reservation.getReservationId())
                    .sessionInfoId(reservation.isReservationNotAllowed() ? null :
                            reservation.getSessionInfo().getSessionInfoId())
                    .isDayOff(reservation.isDayOff())
                    .dayOfWeek(reservation.getDayOfWeek())
                    .reservationDate(reservation.getReservationDate())
                    .status(reservation.getStatus())
                    .memberInfo(reservation.isReservationNotAllowed() ? null :
                            new MemberInfo(reservation.getMember().getMemberId(), reservation.getName()))
                    .build();
        }
    }

    private record MemberInfo(Long memberId, String name) {
    }
}
