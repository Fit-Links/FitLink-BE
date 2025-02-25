package spring.fitlinkbe.interfaces.controller.reservation.dto;

import lombok.Builder;
import spring.fitlinkbe.application.reservation.criteria.ReservationResult;
import spring.fitlinkbe.domain.reservation.Reservation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationResponseDto {


    @Builder(toBuilder = true)
    public record Success(Long reservationId, Reservation.Status status) {
        public static ReservationResponseDto.Success of(Reservation reservation) {

            return Success.builder()
                    .reservationId(reservation.getReservationId())
                    .status(reservation.getStatus())
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record GetList(Long reservationId, Long sessionInfoId,
                          boolean isDayOff, DayOfWeek dayOfWeek, LocalDateTime reservationDate,
                          Reservation.Status status, MemberInfo memberInfo) {

        public static ReservationResponseDto.GetList of(Reservation reservation) {

            return GetList.builder()
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

        private record MemberInfo(Long memberId, String name) {

        }
    }

    @Builder(toBuilder = true)
    public record GetDetail(Long reservationId, Long sessionId,
                            DayOfWeek dayOfWeek, LocalDateTime reservationDate,
                            Reservation.Status status, PersonalInfo memberInfo) {

        public static ReservationResponseDto.GetDetail of(ReservationResult.ReservationDetail result) {

            return GetDetail.builder()
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

        private record PersonalInfo(Long memberId, String name, LocalDate birthDate, String phoneNumber,
                                    String profilePictureUrl) {
        }
    }
}
