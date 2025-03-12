package spring.fitlinkbe.application.member.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.WorkoutSchedule;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.LocalDate;
import java.util.List;

public class MemberInfoResult {

    @Builder
    public record Response(
            Long memberId,
            String name,
            Long trainerId,
            String trainerName,
            ConnectingInfo.ConnectingStatus connectingStatus,
            String profilePictureUrl,
            SessionInfoResponse sessionInfo,
            List<WorkoutScheduleResult.Response> workoutSchedules
    ) {
        public static Response of(Member me, ConnectingInfo connectingInfo, SessionInfo sessionInfo, List<WorkoutSchedule> workoutSchedules) {
            Trainer trainer = connectingInfo != null ? connectingInfo.getTrainer() : null;

            return Response.builder()
                    .memberId(me.getMemberId())
                    .name(me.getName())
                    .trainerId(trainer != null ? trainer.getTrainerId() : null)
                    .trainerName(trainer != null ? trainer.getName() : null)
                    .connectingStatus(connectingInfo != null ? connectingInfo.getStatus() : null)
                    .profilePictureUrl(me.getProfilePictureUrl())
                    .sessionInfo(sessionInfo != null ? SessionInfoResponse.from(sessionInfo) : null)
                    .workoutSchedules(workoutSchedules.stream().map(WorkoutScheduleResult.Response::from).toList())
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

    public record DetailResponse(
            Long memberId,
            String profilePictureUrl,
            String name,
            LocalDate birthDate,
            String phoneNumber
    ) {
        public static DetailResponse from(Member me) {
            return new DetailResponse(
                    me.getMemberId(),
                    me.getProfilePictureUrl(),
                    me.getName(),
                    me.getBirthDate(),
                    me.getPhoneNumber()
            );
        }
    }

    @Builder
    public record SimpleResponse(
            Long memberId,
            String name,
            LocalDate birthDate,
            String phoneNumber,
            String profilePictureUrl,
            SessionInfoResponse sessionInfo
    ) {
        public static SimpleResponse of(Member me, SessionInfo sessionInfo) {
            return SimpleResponse.builder()
                    .memberId(me.getMemberId())
                    .name(me.getName())
                    .birthDate(me.getBirthDate())
                    .phoneNumber(me.getPhoneNumber())
                    .profilePictureUrl(me.getProfilePictureUrl())
                    .sessionInfo(sessionInfo != null ? SessionInfoResponse.from(sessionInfo) : null)
                    .build();
        }
    }
}
