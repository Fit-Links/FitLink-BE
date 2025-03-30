package spring.fitlinkbe.application.reservation.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDateTime;
import java.util.List;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;
import static spring.fitlinkbe.domain.common.enums.UserRole.TRAINER;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.*;

public class ReservationCriteria {

    @Builder(toBuilder = true)
    public record SetDisabledTime(LocalDateTime date) {

        public ReservationCommand.SetDisabledTime toCommand(Long trainerId) {
            return ReservationCommand.SetDisabledTime.builder()
                    .date(date)
                    .trainerId(trainerId)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record ReserveSession(Long trainerId, Long memberId, String name, List<LocalDateTime> dates) {
        public Reservation toDomain(SessionInfo sessionInfo, SecurityUser user) {

            return Reservation.builder()
                    .trainer(Trainer.builder().trainerId(trainerId).build())
                    .member(Member.builder().memberId(memberId).build())
                    .sessionInfo(sessionInfo)
                    .name(name)
                    .reservationDates(dates)
                    .confirmDate(user.getUserRole() == TRAINER ? dates.get(0) : null)
                    .dayOfWeek(dates.get(0).getDayOfWeek())
                    .status(user.getUserRole() == MEMBER ? RESERVATION_WAITING
                            : RESERVATION_APPROVED)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record FixedReserveSession(List<LocalDateTime> reservationDates, Long memberId, String name) {

        public List<Reservation> toDomain(SessionInfo sessionInfo, SecurityUser user) {
            return reservationDates.stream()
                    .map((date) -> Reservation.builder()
                            .member(Member.builder().memberId(memberId).build())
                            .trainer(Trainer.builder().trainerId(user.getTrainerId()).build())
                            .sessionInfo(sessionInfo)
                            .name(name)
                            .reservationDates(List.of(date))
                            .confirmDate(date)
                            .dayOfWeek(date.getDayOfWeek())
                            .status(FIXED_RESERVATION)
                            .build())
                    .toList();
        }
    }

    @Builder(toBuilder = true)
    public record CancelReservation(Long reservationId, LocalDateTime cancelDate, String cancelReason) {
        public ReservationCommand.CancelReservation toCommand() {
            return ReservationCommand.CancelReservation.builder()
                    .reservationId(reservationId)
                    .cancelDate(cancelDate)
                    .cancelReason(cancelReason)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record ApproveReservation(Long reservationId, Long memberId, LocalDateTime reservationDate) {

        public ReservationCommand.ApproveReservation toApproveReservationCommand() {
            return ReservationCommand.ApproveReservation.builder()
                    .reservationId(reservationId)
                    .reservationDate(reservationDate)
                    .build();
        }

        public ReservationCommand.RefuseReservations toRefuseReservationsCommand() {
            return ReservationCommand.RefuseReservations.builder()
                    .reservationDate(reservationDate)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record CompleteSession(Long memberId, Long reservationId, Boolean isJoin) {
        public ReservationCommand.CompleteSession toCompleteSessionCommand() {

            return ReservationCommand.CompleteSession.builder()
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .isJoin(isJoin)
                    .build();
        }

    }

    @Builder(toBuilder = true)
    public record ChangeReqeustReservation(LocalDateTime reservationDate, LocalDateTime changeRequestDate,
                                           Long reservationId) {

        public ReservationCommand.ChangeReqeustReservation toCommand() {
            return ReservationCommand.ChangeReqeustReservation.builder()
                    .reservationId(reservationId)
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record ChangeApproveReservation(Long reservationId, Long memberId, LocalDateTime approveDate,
                                           boolean isApprove) {

        public ReservationCommand.ChangeApproveReservation toCommand() {
            return ReservationCommand.ChangeApproveReservation.builder()
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .isApprove(isApprove)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record CancelApproveReservation(Long reservationId, Long memberId, boolean isApprove) {

        public ReservationCommand.CancelApproveReservation toCommand() {
            return ReservationCommand.CancelApproveReservation.builder()
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .isApprove(isApprove)
                    .build();
        }
    }
}
