package spring.fitlinkbe.domain.reservation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.support.utils.DateUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.*;

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
    private DayOfWeek dayOfWeek;
    private Status status;
    private String cancelReason;
    private boolean isDayOff;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        FIXED_RESERVATION, // 고정 예약
        DISABLED_TIME_RESERVATION, // 예약 불가 시간 설정
        RESERVATION_WAITING, // 예약 대기
        RESERVATION_APPROVED, // 예약 확정
        RESERVATION_CANCELLED, // 예약 취소
        RESERVATION_REJECTED,  // 예약 거부
        RESERVATION_CHANGE_REQUEST //예약 변경 요청
    }

    public boolean checkStatus() {

        return (status != Status.DISABLED_TIME_RESERVATION &&
                status != Status.RESERVATION_CANCELLED &&
                status != Status.RESERVATION_REJECTED);
    }

    public static LocalDateTime getEndDate(LocalDateTime startDate, UserRole userRole) {
        return (userRole == MEMBER) ? DateUtils.getOneMonthAfterDate(startDate)
                : DateUtils.getTwoWeekAfterDate(startDate);
    }

    public boolean isReservationAfterToday() {
        LocalDateTime nowDate = LocalDateTime.now();

        LocalDateTime reservationDate = getReservationDate();

        return reservationDate.isAfter(nowDate);
    }

    public boolean isReservationDateSame(List<LocalDateTime> reservationDates, LocalDateTime requestDate) {

        LocalDateTime truncatedRequestDate = requestDate.truncatedTo(ChronoUnit.HOURS);
        return reservationDates.stream()
                .map(date -> date.truncatedTo(ChronoUnit.HOURS))
                .anyMatch(truncatedRequestDate::isEqual);
    }

    public boolean isWaitingStatus() {
        if (status != Status.RESERVATION_WAITING) {
            throw new CustomException(RESERVATION_IS_NOT_WAITING_STATUS, "예약 상태가 대기 상태가 아닙니다.");
        }

        return true;
    }

    public boolean isReservationInRange(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime reservationDate = getReservationDate();

        return reservationDate.isAfter(startDate)
                && reservationDate.isBefore(endDate);
    }

    public LocalDateTime getReservationDate() {

        if (reservationDates == null) return LocalDateTime.now().minusYears(1);

        return reservationDates.size() == 1 ? reservationDates.get(0)
                : findEarlierDate(reservationDates);
    }

    private LocalDateTime findEarlierDate(List<LocalDateTime> dates) {
        return dates.get(0).isAfter(dates.get(1)) ? dates.get(1) : dates.get(0);
    }

    public void cancel(String message) {
        if (status == Status.RESERVATION_CANCELLED) {
            throw new CustomException(RESERVATION_IS_ALREADY_CANCEL);
        }
        if (status == Status.DISABLED_TIME_RESERVATION || status == Status.RESERVATION_REJECTED) {
            throw new CustomException(RESERVATION_CANCEL_NOT_ALLOWED);
        }
        cancelReason = message;
        status = Status.RESERVATION_CANCELLED;
    }

    public boolean isReservationNotAllowed() {

        return (isDayOff || (status == Status.DISABLED_TIME_RESERVATION));
    }

}
