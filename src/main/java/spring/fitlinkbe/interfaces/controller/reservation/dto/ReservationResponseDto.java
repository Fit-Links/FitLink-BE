package spring.fitlinkbe.interfaces.controller.reservation.dto;

import lombok.Builder;
import spring.fitlinkbe.application.reservation.criteria.ReservationResult;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.Session;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationResponseDto {


    @Builder(toBuilder = true)
    public record Success(Long reservationId, String status) {
        public static ReservationResponseDto.Success of(Reservation reservation) {

            return Success.builder()
                    .reservationId(reservation.getReservationId())
                    .status(reservation.getStatus().getName())
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record SuccessSession(Long sessionId, String status) {
        public static ReservationResponseDto.SuccessSession of(Session session) {

            return SuccessSession.builder()
                    .sessionId(session.getSessionId())
                    .status(session.getStatus().getName())
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record Summary(Long reservationId, Long sessionInfoId,
                          boolean isDayOff, DayOfWeek dayOfWeek, List<LocalDateTime> reservationDates,
                          String status, MemberInfo memberInfo) {

        public static ReservationResponseDto.Summary of(Reservation reservation) {

            return Summary.builder()
                    .reservationId(reservation.getReservationId())
                    .sessionInfoId(reservation.isReservationNotAllowed() ? null :
                            reservation.getSessionInfo().getSessionInfoId())
                    .isDayOff(reservation.isDayOff())
                    .dayOfWeek(reservation.getDayOfWeek())
                    .reservationDates(reservation.getReservationDates())
                    .status(reservation.getStatus().getName())
                    .memberInfo(reservation.isReservationNotAllowed() ? null :
                            new MemberInfo(reservation.getMember().getMemberId(), reservation.getName()))
                    .build();
        }

        private record MemberInfo(Long memberId, String name) {

        }
    }

    @Builder(toBuilder = true)
    public record Detail(Long reservationId, Long sessionId,
                         DayOfWeek dayOfWeek, List<LocalDateTime> reservationDates,
                         Reservation.Status status, PersonalInfo memberInfo) {

        public static ReservationResponseDto.Detail of(ReservationResult.ReservationDetail result) {

            return Detail.builder()
                    .reservationId(result.reservation().getReservationId())
                    .sessionId(result.session() != null ? result.session().getSessionId() : null)
                    .dayOfWeek(result.reservation().getDayOfWeek())
                    .reservationDates(result.reservation().getReservationDates())
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

    @Builder(toBuilder = true)
    public record WaitingMember(Long reservationId, Long memberId, String name,
                                LocalDate birthDate, String phoneNumber, String profilePictureUrl,
                                DayOfWeek dayOfWeek, List<LocalDateTime> reservationDates) {

        public static ReservationResponseDto.WaitingMember of(Reservation reservation) {

            return WaitingMember.builder()
                    .memberId(reservation.getMember().getMemberId())
                    .name(reservation.getMember().getName())
                    .birthDate(reservation.getMember().getBirthDate())
                    .phoneNumber(reservation.getMember().getPhoneNumber())
                    .profilePictureUrl(reservation.getMember().getProfilePictureUrl())
                    .reservationId(reservation.getReservationId())
                    .reservationDates(reservation.getReservationDates())
                    .dayOfWeek(reservation.getReservationDates().get(0).getDayOfWeek())
                    .build();
        }
    }
}
