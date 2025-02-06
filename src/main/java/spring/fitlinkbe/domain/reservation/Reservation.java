package spring.fitlinkbe.domain.reservation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.support.utils.DateUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;

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
    private LocalDateTime reservationDate;
    private LocalDateTime changeDate;
    private DayOfWeek dayOfWeek;
    private Status status;
    private String cancelReason;
    private int priority;
    private boolean isApproved;
    private boolean isFixed;
    private boolean isDisabled;
    private boolean isDayOff;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        RESERVATION_WAITING, // 예약 대기
        RESERVATION_APPROVED, // 예약 확정
        RESERVATION_CANCELLED, // 예약 취소
        RESERVATION_REJECTED,  // 예약 거부
        RESERVATION_CHANGE_REQUEST //예약 변경 요청
    }

    public static LocalDateTime getEndDate(LocalDateTime startDate, UserRole userRole) {
        return (userRole == MEMBER) ? DateUtils.getOneMonthAfterDate(startDate)
                : DateUtils.getTwoWeekAfterDate(startDate);
    }

    public boolean isReservationNotAllowed() {

        return (isDayOff || isDisabled);
    }

}
