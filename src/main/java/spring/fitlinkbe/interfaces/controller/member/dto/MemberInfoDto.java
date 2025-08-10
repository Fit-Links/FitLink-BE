package spring.fitlinkbe.interfaces.controller.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;
import spring.fitlinkbe.application.member.criteria.MemberInfoResult;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class MemberInfoDto {

    @Builder
    public record SimpleResponse(
            Long memberId,
            String name,
            LocalDate birthDate,
            String phoneNumber,
            String profilePictureUrl,
            SessionInfoResponse sessionInfo
    ) {
        public static SimpleResponse from(MemberInfoResult.SimpleResponse result) {
            return SimpleResponse.builder()
                    .memberId(result.memberId())
                    .name(result.name())
                    .birthDate(result.birthDate())
                    .phoneNumber(result.phoneNumber())
                    .profilePictureUrl(result.profilePictureUrl())
                    .sessionInfo(result.sessionInfo() != null ? SessionInfoResponse.from(result.sessionInfo()) : null)
                    .build();
        }
    }

    @Builder
    public record Response(
            Long memberId,
            String name,
            LocalDate birthDate,
            String phoneNumber,
            Long trainerId,
            String trainerName,
            String trainerPhone,
            String trainerProfileUrl,
            ConnectingInfo.ConnectingStatus connectingStatus,
            String profilePictureUrl,
            List<ReservationResponse> fixedReservations,
            SessionInfoResponse sessionInfo,
            List<WorkoutScheduleDto.Response> workoutSchedules
    ) {
        public static Response from(MemberInfoResult.Response result) {
            return Response.builder()
                    .memberId(result.memberId())
                    .name(result.name())
                    .birthDate(result.birthDate())
                    .phoneNumber(result.phoneNumber())
                    .trainerId(result.trainerId())
                    .trainerName(result.trainerName())
                    .trainerPhone(result.trainerPhone())
                    .trainerProfileUrl(result.trainerProfileUrl())
                    .connectingStatus(result.connectingStatus())
                    .profilePictureUrl(result.profilePictureUrl())
                    .fixedReservations(result.fixedReservations().stream().map(ReservationResponse::from).toList())
                    .sessionInfo(result.sessionInfo() != null ? SessionInfoResponse.from(result.sessionInfo()) : null)
                    .workoutSchedules(result.workoutSchedules().stream().map(WorkoutScheduleDto.Response::from).toList())
                    .build();
        }
    }

    @Builder
    public record ReservationResponse(
            Long reservationId,
            LocalDateTime reservationDateTime
    ) {
        public static ReservationResponse from(MemberInfoResult.ReservationResponse result) {
            return ReservationResponse.builder()
                    .reservationId(result.reservationId())
                    .reservationDateTime(result.reservationDateTime())
                    .build();
        }
    }

    @Builder
    public record DetailResponse(
            Long memberId,
            String profilePictureUrl,
            String name,
            LocalDate birthDate,
            String phoneNumber
    ) {
        public static DetailResponse from(MemberInfoResult.DetailResponse result) {
            return DetailResponse.builder()
                    .memberId(result.memberId())
                    .profilePictureUrl(result.profilePictureUrl())
                    .name(result.name())
                    .birthDate(result.birthDate())
                    .phoneNumber(result.phoneNumber())
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

        @JsonIgnore
        @AssertTrue(message = "이름, 전화번호 중 하나는 반드시 입력해야 합니다.")
        private boolean isValid() {
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

