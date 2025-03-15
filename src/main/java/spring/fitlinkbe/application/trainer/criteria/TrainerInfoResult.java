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
}
