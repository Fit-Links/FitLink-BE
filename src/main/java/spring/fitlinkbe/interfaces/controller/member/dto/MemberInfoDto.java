package spring.fitlinkbe.interfaces.controller.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
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


    public record MemberUpdateRequest(
            String name,
            String phoneNumber
    ) {

        @AssertTrue(message = "이름, 전화번호 중 하나는 반드시 입력해야 합니다.")
        @JsonIgnore
        public boolean isValid() {
            return name != null || phoneNumber != null;
        }
    }

    @Builder
    public record MemberUpdateResponse(
            Long memberId,
            String name,
            String phoneNumber
    ) {
        public static MemberUpdateResponse from(MemberInfoResult.MemberUpdateResponse result) {
            return MemberUpdateResponse.builder()
                    .memberId(result.memberId())
                    .name(result.name())
                    .phoneNumber(result.phoneNumber())
                    .build();
        }
    }
}

