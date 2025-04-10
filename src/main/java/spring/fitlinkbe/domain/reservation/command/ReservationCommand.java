package spring.fitlinkbe.domain.reservation.command;

import lombok.Builder;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.reservation.Session;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    public record GetSessions(Long memberId, Long trainerId, Session.Status status, Pageable pageRequest) {
        public static GetSessions of(Long memberId, Session.Status status, Pageable pageRequest) {

            return GetSessions.builder()
                    .memberId(memberId)
                    .status(status)
                    .pageRequest(pageRequest)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record Approve(Long reservationId, LocalDateTime reservationDate) {

    }

    @Builder(toBuilder = true)
    public record RefuseReservations(LocalDateTime reservationDate) {

    }

    @Builder(toBuilder = true)
    public record Cancel(Long reservationId, LocalDateTime cancelDate,
                         String cancelReason) {

    }

    @Builder(toBuilder = true)
    public record Complete(Long reservationId, Long memberId, Boolean isJoin) {
    }

    @Builder(toBuilder = true)
    public record ChangeReqeust(LocalDateTime reservationDate, LocalDateTime changeRequestDate,
                                Long reservationId) {
    }

    @Builder(toBuilder = true)
    public record ChangeApproval(Long reservationId, Long memberId, boolean isApprove) {
    }

    @Builder(toBuilder = true)
    public record CancelApproval(Long reservationId, Long memberId, boolean isApprove) {
    }
}
