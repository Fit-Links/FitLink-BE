package spring.fitlinkbe.interfaces.controller.member.dto;

public class MemberInfoDto {

    public record Response(
            Long memberId,
            Long trainerId,
            String trainerName,
            String profilePictureUrl,
            SessionInfoResponse sessionInfo
    ) {
    }

    public record SessionInfoResponse(
            Long sessionInfoId,
            int totalCount,
            int remainingCount
    ) {
    }
}

