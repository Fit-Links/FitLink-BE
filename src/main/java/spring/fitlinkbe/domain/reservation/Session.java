package spring.fitlinkbe.domain.reservation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.common.exception.CustomException;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.SESSION_IS_ALREADY_CANCEL;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.SESSION_IS_ALREADY_COMPLETED;

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
        SESSION_PROCESSING, // 세션 진행
        SESSION_COMPLETED, // 세션 완료
    }

    public void cancel(String message) {
        if (status == Status.SESSION_CANCELLED) {
            throw new CustomException(SESSION_IS_ALREADY_CANCEL);
        }
        if (status == Status.SESSION_COMPLETED) {
            throw new CustomException(SESSION_IS_ALREADY_COMPLETED);
        }
        cancelReason = message;
        status = Status.SESSION_CANCELLED;
    }


}