package spring.fitlinkbe.domain.reservation;


import lombok.*;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.support.utils.DateUtils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.*;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private Long reservationId;
    private Member member;
    private Trainer trainer;
    private SessionInfo sessionInfo;
    private String name;
    private List<LocalDateTime> reservationDates;
    private LocalDateTime changeDate;
    private LocalDateTime confirmDate;
    private DayOfWeek dayOfWeek;
    private Status status;
    private String cancelReason;
    private boolean isDayOff;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @RequiredArgsConstructor
    @Getter
    public enum Status {
        FIXED_RESERVATION("고정 예약"), // 고정 예약
        DISABLED_TIME_RESERVATION("예약 불가 설정"), // 예약 불가 시간 설정
        RESERVATION_WAITING("예약 대기"), // 예약 대기
        RESERVATION_APPROVED("예약 확정"), // 예약 확정
        RESERVATION_CANCELLED("예약 취소"), // 예약 취소
        RESERVATION_CANCEL_REQUEST("예약 취소 요청"), // 예약 취소
        RESERVATION_CANCEL_REQUEST_REFUSED("예약 취소 거절"), //예약 변경 거절
        RESERVATION_REFUSED("예약 거절"),  // 예약 거부
        RESERVATION_CHANGE_REQUEST("예약 변경 요청"), //예약 변경 요청
        RESERVATION_CHANGE_REQUEST_REFUSED("예약 변경 거절"), //예약 변경 거절
        RESERVATION_COMPLETED("예약 종료"); // 세션까지 완전 완료 되었을 때

        private final String name;
    }

    public static List<Reservation> createFixedReservations(List<Reservation> baseReservations, int remainingCount) {
        List<Reservation> generatedReservations = new ArrayList<>();
        List<Reservation> currentReservations = new ArrayList<>(baseReservations);

        while (generatedReservations.size() < remainingCount) {
            int remainingToCreate = remainingCount - generatedReservations.size();

            List<Reservation> nextReservations = currentReservations.stream()
                    .map(reservation -> reservation.copyWithNewDate(reservation.getReservationDate().plusDays(7)))
                    .limit(remainingToCreate)
                    .toList();

            generatedReservations.addAll(nextReservations);
            currentReservations = nextReservations;
        }

        return generatedReservations;
    }

    public boolean isFixedWithBaseDate(LocalDateTime baseDate) {
        if (confirmDate == null || baseDate == null) {
            throw new CustomException(RESERVATION_RELEASE_NOT_ALLOWED);
        }

        LocalDateTime truncatedBaseDate = baseDate.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime truncatedReservationDate = confirmDate.truncatedTo(ChronoUnit.HOURS);

        Duration duration = Duration.between(truncatedBaseDate, truncatedReservationDate);

        return !duration.isNegative() && duration.toHours() % (7 * 24) == 0;
    }

    public Reservation copyWithNewDate(LocalDateTime newDate) {

        return Reservation.builder()
                .member(member)
                .trainer(trainer)
                .sessionInfo(sessionInfo)
                .name(name)
                .reservationDates(List.of(newDate))
                .confirmDate(newDate)
                .dayOfWeek(newDate.getDayOfWeek())
                .status(status)
                .build();
    }


    public void changeFixedDate(LocalDateTime beforeDate, LocalDateTime changeDate) {
        if (this.status != FIXED_RESERVATION) {
            throw new CustomException(RESERVATION_CHANGE_REQUEST_NOT_ALLOWED, "고정 예약 상태가 아닙니다.");
        }

        LocalDateTime targetDate = beforeDate.truncatedTo(ChronoUnit.HOURS);

        boolean dateExists = this.reservationDates.stream()
                .map(date -> date.truncatedTo(ChronoUnit.HOURS))
                .anyMatch(targetDate::isEqual);

        if (!dateExists) {
            throw new CustomException(RESERVATION_DATE_NOT_FOUND);
        }

        this.reservationDates = List.of(changeDate);
        this.confirmDate = changeDate;
    }


    public void changeRequestDate(LocalDateTime reservationDate, LocalDateTime changeDate) {
        // 변경할 시간은 현재시간 +2시간 이후부터 변경 가능
        if (changeDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new CustomException(RESERVATION_CHANGE_REQUEST_NOT_ALLOWED, "현재시간보다 2시간 이후부터 변경 가능합니다.");
        }

        if (this.status != RESERVATION_APPROVED && this.status != RESERVATION_WAITING) {
            throw new CustomException(RESERVATION_CHANGE_REQUEST_NOT_ALLOWED);
        }

        LocalDateTime targetDate = reservationDate.truncatedTo(ChronoUnit.HOURS);

        boolean dateExists = this.reservationDates.stream()
                .map(date -> date.truncatedTo(ChronoUnit.HOURS))
                .anyMatch(targetDate::isEqual);

        if (!dateExists) {
            throw new CustomException(RESERVATION_DATE_NOT_FOUND);
        }

        this.changeDate = changeDate;
        this.status = RESERVATION_CHANGE_REQUEST;
    }

    public void approveChangeReqeust(Long memberId, boolean isApprove) {

        if (!Objects.equals(this.member.getMemberId(), memberId)) {
            throw new CustomException(MEMBER_NOT_FOUND, "잘못된 멤버의 예약을 변경하려고 합니다.");
        }

        if (this.status != RESERVATION_CHANGE_REQUEST) {
            throw new CustomException(RESERVATION_APPROVE_NOT_ALLOWED, "예약 변경 승인을 할 수 있는 상태가 아닙니다.");
        }

        this.confirmDate = isApprove ? this.changeDate : null;
        this.status = isApprove ? RESERVATION_APPROVED : RESERVATION_CHANGE_REQUEST_REFUSED;
    }


    public void approveCancelReqeust(Long memberId, boolean isApprove) {
        if (!Objects.equals(this.member.getMemberId(), memberId)) {
            throw new CustomException(MEMBER_NOT_FOUND, "잘못된 멤버의 예약을 변경하려고 합니다.");
        }

        if (this.status != RESERVATION_CANCEL_REQUEST) {
            throw new CustomException(RESERVATION_APPROVE_NOT_ALLOWED, "예약 취소 승인을 할 수 있는 상태가 아닙니다.");
        }

        this.status = isApprove ? RESERVATION_CANCELLED : RESERVATION_CANCEL_REQUEST_REFUSED;
    }

    public void checkDisableStatus() {
        if (this.status != DISABLED_TIME_RESERVATION) {
            throw new CustomException(SET_DISABLE_DATE_FAILED, "예약 불가 해지할 수 있는 상태가 아닙니다.");
        }
    }

    public static LocalDateTime getEndDate(LocalDateTime startDate, UserRole userRole) {
        return (userRole == MEMBER) ? DateUtils.getOneMonthAfterDate(startDate)
                : DateUtils.getTwoWeekAfterDate(startDate);
    }

    public boolean isReservationDateSame(List<LocalDateTime> requestDates) {
        return requestDates
                .stream()
                .anyMatch(reqDate -> {
                    LocalDateTime truncatedRequestDate = reqDate.truncatedTo(ChronoUnit.HOURS);
                    return this.reservationDates.stream()
                            .map(date -> date.truncatedTo(ChronoUnit.HOURS))
                            .anyMatch(truncatedRequestDate::isEqual);
                });
    }

    public boolean isTodayReservation() {
        LocalDate today = LocalDate.now();

        return reservationDates
                .stream()
                .map(LocalDateTime::toLocalDate)
                .anyMatch(today::isEqual);
    }

    public boolean isReservationInRange(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime reservationDate = getReservationDate();

        return reservationDate.isAfter(startDate)
                && reservationDate.isBefore(endDate);
    }

    public LocalDateTime getReservationDate() {

        if (this.reservationDates == null) {
            throw new CustomException(RESERVATION_DATE_NOT_FOUND);
        }

        return this.reservationDates.size() == 1 ? this.reservationDates.get(0)
                : findEarlierDate(this.reservationDates);
    }

    private LocalDateTime findEarlierDate(List<LocalDateTime> dates) {
        return dates.get(0).isAfter(dates.get(1)) ? dates.get(1) : dates.get(0);
    }

    public Reservation refuse() {
        this.status = RESERVATION_REFUSED;

        return this;
    }

    public Reservation approve(LocalDateTime reservationDate) {
        if (this.status == DISABLED_TIME_RESERVATION || this.status == RESERVATION_REFUSED) {
            throw new CustomException(RESERVATION_APPROVE_NOT_ALLOWED);
        }
        if (this.status == RESERVATION_APPROVED) {
            throw new CustomException(RESERVATION_IS_ALREADY_APPROVED);
        }

        if (this.reservationDates.size() > 1) {
            this.reservationDates = List.of(reservationDate);
        }

        this.confirmDate = reservationDate;
        this.status = RESERVATION_APPROVED;

        return this;
    }

    public Reservation complete(Long trainerId, Long memberId) {
        if (!trainerId.equals(this.trainer.getTrainerId()) || !memberId.equals(this.member.getMemberId())) {
            throw new CustomException(RESERVATION_COMPLETE_NOT_ALLOWED);
        }

        if (this.status == RESERVATION_COMPLETED) {
            throw new CustomException(RESERVATION_IS_ALREADY_COMPLETED);
        }

        this.status = RESERVATION_COMPLETED;

        return this;
    }

    public Reservation cancel(String message) {
        validateCancellable();

        this.cancelReason = message;
        this.confirmDate = null;
        this.status = RESERVATION_CANCELLED;

        return this;
    }

    public void cancel(String message, LocalDateTime cancelDate) {
        if (!isReservationDateSame(List.of(cancelDate))) {
            throw new CustomException(RESERVATION_DATE_NOT_FOUND);
        }

        validateCancellable();
        validateNotSameDayCancellation(cancelDate);

        this.cancelReason = message;
        this.confirmDate = null;
        this.status = RESERVATION_CANCELLED;
    }

    public void cancelRequest(String message, LocalDateTime cancelDate) {
        if (!isReservationDateSame(List.of(cancelDate))) {
            throw new CustomException(RESERVATION_DATE_NOT_FOUND);
        }

        validateCancellable();

        if (this.status != RESERVATION_WAITING) {
            validateNotSameDayCancellation(cancelDate);
        }

        this.cancelReason = message;
        this.confirmDate = (this.status == RESERVATION_WAITING) ? null : this.confirmDate;
        this.status = (this.status == RESERVATION_WAITING) ? RESERVATION_CANCELLED : RESERVATION_CANCEL_REQUEST;
    }

    public boolean isReservationNotAllowed() {
        return (this.isDayOff || (this.status == DISABLED_TIME_RESERVATION));
    }

    private static void validateNotSameDayCancellation(LocalDateTime cancelDate) {
        if (LocalDateTime.now().isAfter(cancelDate.minusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(23))) {
            throw new CustomException(RESERVATION_CANCEL_NOT_ALLOWED, "당일 예약 취소 요청은 불가합니다.");
        }
    }

    private void validateCancellable() {
        if (this.status == RESERVATION_CANCELLED) {
            throw new CustomException(RESERVATION_IS_ALREADY_CANCEL);
        }

        if (this.status == DISABLED_TIME_RESERVATION || this.status == RESERVATION_REFUSED) {
            throw new CustomException(RESERVATION_CANCEL_NOT_ALLOWED);
        }
    }
}
