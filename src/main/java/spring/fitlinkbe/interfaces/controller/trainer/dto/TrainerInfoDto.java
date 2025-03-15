package spring.fitlinkbe.interfaces.controller.trainer.dto;

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
}
