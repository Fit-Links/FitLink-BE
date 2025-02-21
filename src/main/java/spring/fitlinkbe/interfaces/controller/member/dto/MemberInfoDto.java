package spring.fitlinkbe.interfaces.controller.member.dto;

import lombok.Builder;
import spring.fitlinkbe.application.member.criteria.MemberInfoResult;

public class MemberInfoDto {

    @Builder
    public record Response(
            Long memberId,
            String name,
            Long trainerId,
            String trainerName,
            String profilePictureUrl,
            SessionInfoResponse sessionInfo
    ) {
        public static Response from(MemberInfoResult.Response result) {
            return Response.builder()
                    .memberId(result.memberId())
                    .name(result.name())
                    .trainerId(result.trainerId())
                    .trainerName(result.trainerName())
                    .profilePictureUrl(result.profilePictureUrl())
                    .sessionInfo(result.sessionInfo() != null ? SessionInfoResponse.from(result.sessionInfo()) : null)
                    .build();
        }
    }

    public record SessionInfoResponse(
            Long sessionInfoId,
            int totalCount,
            int remainingCount
    ) {
        public static SessionInfoResponse from(MemberInfoResult.SessionInfoResponse result) {
            return new SessionInfoResponse(
                    result.sessionInfoId(),
                    result.totalCount(),
                    result.remainingCount()
            );
        }
    }
}

