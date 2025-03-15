package spring.fitlinkbe.application.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.application.trainer.criteria.TrainerInfoResult;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;

@Component
@RequiredArgsConstructor
public class TrainerFacade {
    private final TrainerService trainerService;

    public TrainerInfoResult.Response getTrainerInfo(Long trainerId) {
        Trainer trainer = trainerService.getTrainerInfo(trainerId);
        PersonalDetail personalDetail = trainerService.getTrainerDetail(trainerId);

        return TrainerInfoResult.Response.of(trainer, personalDetail);
    }
}
