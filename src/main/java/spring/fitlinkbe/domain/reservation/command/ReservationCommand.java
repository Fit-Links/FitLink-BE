package spring.fitlinkbe.domain.reservation.command;

import lombok.Builder;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.reservation.Session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationCommand {


    @Builder(toBuilder = true)
    public record SetDisabledTime(LocalDateTime date, Long trainerId) {

    }

    @Builder(toBuilder = true)
    public record GetReservations(LocalDate date, UserRole role, Long userId) {
        public static GetReservations of(LocalDate date, UserRole userRole, Long userId) {

            return GetReservations.builder()
                    .date(date)
                    .role(userRole)
                    .userId(userId)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record GetReservationThatTimes(List<LocalDateTime> date, Long trainerId) {

    }

    @Builder(toBuilder = true)
    public record GetSessions(Long memberId, Session.Status status, Pageable pageRequest) {
        public static GetSessions of(Long memberId, Session.Status status, Pageable pageRequest) {

            return GetSessions.builder()
                    .memberId(memberId)
                    .status(status)
                    .pageRequest(pageRequest)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record CancelReservation(Long reservationId, String cancelReason) {

    }
}
