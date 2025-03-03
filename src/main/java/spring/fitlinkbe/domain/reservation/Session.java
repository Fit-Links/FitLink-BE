package spring.fitlinkbe.domain.reservation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    public enum Status {

        SESSION_CANCELLED, // 세션 취소
        SESSION_WAITING, // 세션 대기
        SESSION_NOT_ATTEND, // 세션 불참석
        SESSION_COMPLETED, // 세션 완료

        NO_SHOW // 노쇼
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


}