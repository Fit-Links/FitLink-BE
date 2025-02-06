package spring.fitlinkbe.domain.reservation;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}