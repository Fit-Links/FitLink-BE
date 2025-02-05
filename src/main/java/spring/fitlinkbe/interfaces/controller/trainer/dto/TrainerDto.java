package spring.fitlinkbe.interfaces.controller.trainer.dto;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.trainer.Trainer;

public class TrainerDto {

    @Builder(toBuilder = true)
    public record Response(Long trainerId, PersonalDetail personalDetail, String trainerCode) {

        public static Response of(Trainer trainer) {

            return Response.builder()
                    .trainerId(trainer.getTrainerId())
                    .trainerCode(trainer.getTrainerCode())
                    .build();
        }
    }
}
