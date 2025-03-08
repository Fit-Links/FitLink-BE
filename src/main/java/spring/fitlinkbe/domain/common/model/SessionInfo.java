package spring.fitlinkbe.domain.common.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
     * 세션 정보 업데이트, totalCount 와 remainingCount 가 0 이상이면서 null 이 아닌 경우에만 업데이트
     */
    public void update(Integer totalCount, Integer remainingCount) {
        if (totalCount != null && totalCount >= 0) {
            this.totalCount = totalCount;
        }
        if (remainingCount != null && remainingCount >= 0) {
            this.remainingCount = remainingCount;
        }
    }
}
