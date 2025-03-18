package spring.fitlinkbe.application.trainer.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.LocalDate;

public class TrainerInfoResult {

    @Builder
    public record Response(
            Long trainerId,
            String name,
            LocalDate birthDate,
            String phoneNumber,
            String profilePictureUrl
    ) {
        public static Response of(Trainer trainer, PersonalDetail personalDetail) {
            return Response.builder()
                    .trainerId(trainer.getTrainerId())
                    .name(personalDetail.getName())
                    .birthDate(personalDetail.getBirthDate())
                    .phoneNumber(personalDetail.getPhoneNumber())
                    .profilePictureUrl(personalDetail.getProfilePictureUrl())
                    .build();
        }
    }

    @Builder
    public record TrainerUpdateRequest(
            String name,
            String phoneNumber
    ) {
        public void updateTrainer(Trainer trainer, PersonalDetail personalDetail) {
            if (name != null) {
                trainer.updateName(name);
                personalDetail.updateName(name);
            }

            if (phoneNumber != null) {
                personalDetail.updatePhoneNumber(phoneNumber);
            }
        }
    }

    @Builder
    public record TrainerUpdateResponse(
            Long trainerId,
            String name,
            String phoneNumber
    ) {
        public static Response of(Trainer trainer, PersonalDetail personalDetail) {
            return Response.builder()
                    .trainerId(trainer.getTrainerId())
                    .name(personalDetail.getName())
                    .phoneNumber(personalDetail.getPhoneNumber())
                    .build();
        }
    }

    @Builder
    public record TrainerCodeResponse(
            String trainerCode
    ) {
        public static TrainerCodeResponse from(String trainerCode) {
            return TrainerCodeResponse.builder()
                    .trainerCode(trainerCode)
                    .build();
        }
    }
}
