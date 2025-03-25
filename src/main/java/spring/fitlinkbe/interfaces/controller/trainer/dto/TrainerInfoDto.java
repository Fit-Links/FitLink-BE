package spring.fitlinkbe.interfaces.controller.trainer.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;
import spring.fitlinkbe.application.trainer.criteria.TrainerInfoResult;

import java.time.LocalDate;

public class TrainerInfoDto {

    @Builder
    public record Response(
            Long trainerId,
            String name,
            LocalDate birthDate,
            String phoneNumber,
            String profilePictureUrl
    ) {
        public static Response from(TrainerInfoResult.Response response) {
            return Response.builder()
                    .trainerId(response.trainerId())
                    .name(response.name())
                    .birthDate(response.birthDate())
                    .phoneNumber(response.phoneNumber())
                    .profilePictureUrl(response.profilePictureUrl())
                    .build();
        }
    }

    public record TrainerUpdateRequest(
            String name,
            String phoneNumber
    ) {
        public TrainerInfoResult.TrainerUpdateRequest toRequest() {
            return TrainerInfoResult.TrainerUpdateRequest.builder()
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .build();
        }

        @AssertTrue(message = "이름과 전화번호 중 하나는 반드시 있어야 합니다.")
        boolean isNameAndPhoneNumberNotNull() {
            return name != null || phoneNumber != null;
        }
    }

    @Builder
    public record TrainerUpdateResponse(
            Long trainerId,
            String name,
            String phoneNumber
    ) {
        public static TrainerUpdateResponse from(TrainerInfoResult.Response response) {
            return TrainerUpdateResponse.builder()
                    .trainerId(response.trainerId())
                    .name(response.name())
                    .phoneNumber(response.phoneNumber())
                    .build();
        }
    }

    @Builder
    public record TrainerCodeResponse(
            String trainerCode
    ) {
        public static TrainerCodeResponse from(TrainerInfoResult.TrainerCodeResponse response) {
            return TrainerCodeResponse.builder()
                    .trainerCode(response.trainerCode())
                    .build();
        }
    }

}
