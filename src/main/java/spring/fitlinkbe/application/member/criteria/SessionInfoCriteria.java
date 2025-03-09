package spring.fitlinkbe.application.member.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.SessionInfo;

public class SessionInfoCriteria {

    @Builder
    public record UpdateRequest(
            Integer remainingCount,

            Integer totalCount
    ) {
        public void patch(SessionInfo sessionInfo) {
            if (remainingCount != null) {
                sessionInfo.updateRemainingCount(remainingCount);
            }
            if (totalCount != null) {
                sessionInfo.updateTotalCount(totalCount);
            }
        }
    }

    public record Response(
            Long sessionInfoId,
            Integer remainingCount,
            Integer totalCount
    ) {
        public static Response from(SessionInfo sessionInfo) {
            return new Response(
                    sessionInfo.getSessionInfoId(),
                    sessionInfo.getRemainingCount(),
                    sessionInfo.getTotalCount()
            );
        }
    }
}
