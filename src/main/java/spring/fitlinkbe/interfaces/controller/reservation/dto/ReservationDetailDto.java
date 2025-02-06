package spring.fitlinkbe.interfaces.controller.reservation.dto;

import lombok.Builder;
import spring.fitlinkbe.application.reservation.ReservationResult;
import spring.fitlinkbe.domain.reservation.Reservation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationDetailDto {

    @Builder(toBuilder = true)
    public record Response(Long reservationId, Long sessionId,
                           DayOfWeek dayOfWeek, LocalDateTime reservationDate,
                           Reservation.Status status, PersonalInfo memberInfo) {

        public static ReservationDetailDto.Response of(ReservationResult.ReservationDetail result) {

            return Response.builder()
                    .reservationId(result.reservation().getReservationId())
                    .sessionId(result.session() != null ? result.session().getSessionId() : null)
                    .dayOfWeek(result.reservation().getDayOfWeek())
                    .reservationDate(result.reservation().getReservationDate())
                    .status(result.reservation().getStatus())
                    .memberInfo(new PersonalInfo(result.personalDetail().getMemberId(),
                            result.reservation().getName(),
                            result.reservation().getMember().getBirthDate(),
                            result.personalDetail().getPhoneNumber(),
                            result.personalDetail().getProfilePictureUrl()))
                    .build();
        }
    }

    private record PersonalInfo(Long memberId, String name, LocalDate birthDate, String phoneNumber,
                                String profilePictureUrl) {
    }
}
