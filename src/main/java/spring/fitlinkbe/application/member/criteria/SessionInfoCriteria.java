package spring.fitlinkbe.application.member.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.SessionInfo;

public class SessionInfoCriteria {

    @Builder
    public record UpdateRequest(
            Integer remainingCount,

            Integer totalCount
    ) {
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
