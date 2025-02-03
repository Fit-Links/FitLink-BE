package spring.fitlinkbe.interfaces.controller.reservation.dto;

import lombok.Builder;
import spring.fitlinkbe.domain.reservation.Reservation;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class ReservationDto {


    @Builder(toBuilder = true)
    public record Response(Long reservationId, Long sessionInfoId,
                           boolean isDayOff, DayOfWeek dayOfWeek, LocalDateTime reservationDate,
                           Reservation.Status status, int priority, MemberInfo memberInfo) {

        public static ReservationDto.Response of(Reservation reservation) {

            return Response.builder()
                    .reservationId(reservation.getReservationId())
                    .sessionInfoId(reservation.getSessionInfoId())
                    .isDayOff(reservation.isDayOff())
                    .dayOfWeek(reservation.getDayOfWeek())
                    .reservationDate(reservation.getReservationDate())
                    .priority(reservation.getPriority())
                    .status(reservation.getStatus())
                    .memberInfo(new MemberInfo(reservation.getMemberId(), reservation.getName()))
                    .build();
        }
    }

    private record MemberInfo(Long memberId, String name) {
    }
}
