package spring.fitlinkbe.domain.reservation;


import lombok.*;
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
    private DayOfWeek dayOfWeek;
    private Status status;
    private String cancelReason;
    private boolean isDayOff;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Reservation toFixedDomain() {

        return Reservation.builder()
                .member(member)
                .trainer(trainer)
                .sessionInfo(sessionInfo)
                .name(name)
                .reservationDates(getAfterSevenDay())
                .changeDate(changeDate)
                .dayOfWeek(dayOfWeek)
                .status(FIXED_RESERVATION)
                .isDayOff(isDayOff)
                .createdAt(createdAt)
                .build();
    }

    public List<LocalDateTime> getAfterSevenDay() {
        return reservationDates.stream().map(date -> date.plusDays(7)).toList();
    }


    @RequiredArgsConstructor
    @Getter
    public enum Status {
        FIXED_RESERVATION("고정 예약"), // 고정 예약
        DISABLED_TIME_RESERVATION("예약 불가 설정"), // 예약 불가 시간 설정
        RESERVATION_WAITING("예약 대기"), // 예약 대기
        RESERVATION_APPROVED("예약 확정"), // 예약 확정
        RESERVATION_CANCELLED("예약 취소"), // 예약 취소
        RESERVATION_CANCEL_REQUEST("예약 취소 요청"), // 예약 취소
        RESERVATION_REFUSED("예약 거절"),  // 예약 거부
        RESERVATION_CHANGE_REQUEST("예약 변경 요청"), //예약 변경 요청
        RESERVATION_COMPLETED("예약 종료"); // 세션까지 완전 완료 되었을 때

        private final String name;
    }

    public boolean isAlreadyCancel() {
        return status != RESERVATION_CANCELLED && status != RESERVATION_REFUSED;
    }

    public void checkPossibleReserveStatus() {
        if (status == DISABLED_TIME_RESERVATION || status == RESERVATION_COMPLETED) {
            throw new CustomException(RESERVATION_NOT_ALLOWED);
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
                    return reservationDates.stream()
                            .map(date -> date.truncatedTo(ChronoUnit.HOURS))
                            .anyMatch(truncatedRequestDate::isEqual);
                });
    }

    public boolean isWaitingStatus() {
        if (status != RESERVATION_WAITING) {
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

    public void refuse() {

        this.status = RESERVATION_REFUSED;
    }

    public void cancel(String message) {
        if (status == RESERVATION_CANCELLED) {
            throw new CustomException(RESERVATION_IS_ALREADY_CANCEL);
        }
        if (status == DISABLED_TIME_RESERVATION || status == RESERVATION_REFUSED) {
            throw new CustomException(RESERVATION_CANCEL_NOT_ALLOWED);
        }
        cancelReason = message;
        this.status = RESERVATION_CANCELLED;
    }

    public void approve(LocalDateTime reservationDate) {
        if (status == DISABLED_TIME_RESERVATION || status == RESERVATION_REFUSED) {
            throw new CustomException(RESERVATION_APPROVE_NOT_ALLOWED);
        }
        if (status == RESERVATION_APPROVED) {
            throw new CustomException(RESERVATION_IS_ALREADY_APPROVED);
        }

        if (reservationDates.size() > 1) {
            reservationDates = List.of(reservationDate);
        }

        this.status = RESERVATION_APPROVED;
    }


    public void cancelRequest(String message) {
        // 당일 예약 취소는 불가능 함
        if (LocalDateTime.now().isAfter(reservationDates.get(0).minusDays(1).truncatedTo(ChronoUnit.DAYS).plusHours(23))) {
            throw new CustomException(RESERVATION_CANCEL_NOT_ALLOWED, "당일 예약 취소 요청은 불가합니다.");
        }
        if (status == DISABLED_TIME_RESERVATION || status == RESERVATION_REFUSED) {
            throw new CustomException(RESERVATION_CANCEL_NOT_ALLOWED);
        }
        if (status == RESERVATION_CANCELLED) {
            throw new CustomException(RESERVATION_IS_ALREADY_CANCEL);
        }
        cancelReason = message;
        this.status = RESERVATION_CANCEL_REQUEST;

    }

    public boolean isReservationNotAllowed() {

        return (isDayOff || (status == DISABLED_TIME_RESERVATION));
    }

}
