package spring.fitlinkbe.application.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public TrainerInfoResult.Response updateTrainerInfo(
            Long trainerId,
            TrainerInfoResult.TrainerUpdateRequest request
    ) {
        Trainer trainer = trainerService.getTrainerInfo(trainerId);
        PersonalDetail personalDetail = trainerService.getTrainerDetail(trainerId);

        if (request.name() != null) {
            trainer.updateName(request.name());
            personalDetail.updateName(request.name());
        }

        if (request.phoneNumber() != null) {
            personalDetail.updatePhoneNumber(request.phoneNumber());
        }
        trainerService.saveTrainer(trainer);
        trainerService.savePersonalDetail(personalDetail);

        return TrainerInfoResult.TrainerUpdateResponse.of(trainer, personalDetail);
    }

    public TrainerInfoResult.TrainerCodeResponse getTrainerCode(Long trainerId) {
        Trainer trainer = trainerService.getTrainerInfo(trainerId);

        return TrainerInfoResult.TrainerCodeResponse.from(trainer.getTrainerCode());
    }
}
