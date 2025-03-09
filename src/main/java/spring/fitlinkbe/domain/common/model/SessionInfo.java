package spring.fitlinkbe.domain.common.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.trainer.Trainer;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {

    private Long SessionInfoId;
    private Member member;
    private Trainer trainer;
    private int totalCount;
    private int remainingCount;

    /**
     * 남은 PT 횟수 업데이트
     *
     * @param count 업데이트 할 횟수
     * @throws spring.fitlinkbe.domain.common.exception.CustomException count 가 0보다 작을 경우
     */
    public void updateRemainingCount(int count) {
        if (count < 0) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER, "남은 횟수는 0보다 작을 수 없습니다. [count: %d]".formatted(count));
        }
        remainingCount = count;
    }

    /**
     * 총 PT 횟수 업데이트 </br>
     *
     * @param count 업데이트 할 횟수
     * @throws spring.fitlinkbe.domain.common.exception.CustomException count 가 0보다 작을 경우
     */
    public void updateTotalCount(int count) {
        if (count < 0) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER, "총 횟수는 0보다 작을 수 없습니다. [count: %d]".formatted(count));
        }
        totalCount = count;
    }
}
