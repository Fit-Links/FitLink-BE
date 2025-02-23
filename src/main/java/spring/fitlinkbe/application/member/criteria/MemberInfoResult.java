package spring.fitlinkbe.application.member.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.trainer.Trainer;

public class MemberInfoResult {

    @Builder
    public record Response(
            Long memberId,
            String name,
            Long trainerId,
            String trainerName,
            String profilePictureUrl,
            SessionInfoResponse sessionInfo
    ) {
        public static Response of(Member me, Trainer trainer, SessionInfo sessionInfo) {
            return Response.builder()
                    .memberId(me.getMemberId())
                    .name(me.getName())
                    .trainerId(trainer != null ? trainer.getTrainerId() : null)
                    .trainerName(trainer != null ? trainer.getName() : null)
                    .profilePictureUrl(me.getProfilePictureUrl())
                    .sessionInfo(sessionInfo != null ? SessionInfoResponse.from(sessionInfo) : null)
                    .build();
        }
    }

    @Builder
    public record SessionInfoResponse(
            Long sessionInfoId,
            int totalCount,
            int remainingCount
    ) {
        public static SessionInfoResponse from(SessionInfo sessionInfo) {
            return SessionInfoResponse.builder()
                    .sessionInfoId(sessionInfo.getSessionInfoId())
                    .totalCount(sessionInfo.getTotalCount())
                    .remainingCount(sessionInfo.getRemainingCount())
                    .build();
        }
    }

    @Builder
    public record MemberUpdateResponse(
            Long memberId,
            String name,
            String phoneNumber
    ) {
        public static MemberUpdateResponse from(Member me) {
            return MemberUpdateResponse.builder()
                    .memberId(me.getMemberId())
                    .name(me.getName())
                    .phoneNumber(me.getPhoneNumber())
                    .build();
        }
    }
}
