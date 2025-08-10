package spring.fitlinkbe.application.trainer.criteria;

import lombok.Builder;

@Builder
public record ConnectRequestDecisionResult(
        Long memberId,
        Long sessionInfoId
) {
    public static ConnectRequestDecisionResult of(Long memberId, Long sessionInfoId) {
        return ConnectRequestDecisionResult.builder()
                .memberId(memberId)
                .sessionInfoId(sessionInfoId)
                .build();
    }
}
