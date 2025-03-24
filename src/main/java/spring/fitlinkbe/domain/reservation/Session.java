package spring.fitlinkbe.domain.reservation;


import lombok.*;
import spring.fitlinkbe.domain.common.exception.CustomException;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.SESSION_IS_ALREADY_CANCEL;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.SESSION_IS_ALREADY_END;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    private Long sessionId;
    private Reservation reservation;
    private Status status;
    private String cancelReason;
    private boolean isCompleted;

    @RequiredArgsConstructor
    @Getter
    public enum Status {

        SESSION_CANCELLED("세션 취소"), // 세션 취소
        SESSION_WAITING("세션 대기"), // 세션 대기
        SESSION_NOT_ATTEND("세션 불참석"), // 세션 불참석
        SESSION_COMPLETED("세션 완료"); // 세션 완료

        private final String name;
    }

    public void cancel(String message) {
        if (status == Status.SESSION_CANCELLED) {
            throw new CustomException(SESSION_IS_ALREADY_CANCEL);
        }
        if (status == Status.SESSION_NOT_ATTEND || status == Status.SESSION_COMPLETED) {
            throw new CustomException(SESSION_IS_ALREADY_END);
        }
        cancelReason = message;
        status = Status.SESSION_CANCELLED;
    }

    public void complete(boolean join) {
        if (status == Status.SESSION_NOT_ATTEND || status == Status.SESSION_COMPLETED) {
            throw new CustomException(SESSION_IS_ALREADY_END);
        }

        status = join ? Status.SESSION_COMPLETED : Status.SESSION_NOT_ATTEND;
    }


}