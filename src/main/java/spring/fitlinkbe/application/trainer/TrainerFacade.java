package spring.fitlinkbe.application.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.application.trainer.criteria.AvailableTimeCriteria;
import spring.fitlinkbe.application.trainer.criteria.AvailableTimesResult;
import spring.fitlinkbe.application.trainer.criteria.TrainerInfoResult;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;

import java.time.LocalDate;
import java.util.List;

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

    public AvailableTimesResult.Response getAvailableTimes(Long trainerId) {
        List<AvailableTime> currentSchedules = trainerService.getCurrentAvailableTimes(trainerId);
        List<AvailableTime> scheduledSchedules = trainerService.getScheduledAvailableTimes(trainerId);

        return AvailableTimesResult.Response.of(currentSchedules, scheduledSchedules);
    }

    @Transactional
    public void saveAvailableTimes(Long trainerId, AvailableTimeCriteria.AddRequest criteria) {
        Trainer trainer = trainerService.getTrainerInfo(trainerId);

        if (criteria.applyAt().equals(LocalDate.now())) {
            saveCurrentApplyAvailableTimes(trainer, criteria);
        } else {
            saveScheduledAvailableTimes(trainer, criteria);
        }
    }

    private void saveCurrentApplyAvailableTimes(Trainer trainer, AvailableTimeCriteria.AddRequest criteria) {
        if (!trainerService.getCurrentAvailableTimes(trainer.getTrainerId()).isEmpty()) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED_AVAILABLE_TIMES);
        }
        List<AvailableTime> availableTimes = criteria.toAvailableTimes(trainer);
        trainerService.saveAvailableTimes(availableTimes);
    }

    private void saveScheduledAvailableTimes(Trainer trainer, AvailableTimeCriteria.AddRequest criteria) {
        if (!trainerService.getScheduledAvailableTimes(trainer.getTrainerId()).isEmpty()) {
            throw new CustomException(ErrorCode.ALREADY_SCHEDULED_AVAILABLE_TIMES);
        }
        List<AvailableTime> availableTimes = criteria.toAvailableTimes(trainer);
        trainerService.saveAvailableTimes(availableTimes);
    }

    public void deleteAvailableTimes(Long trainerId, LocalDate applyAt) {
        List<AvailableTime> availableTimes = trainerService.getAvailableTimes(trainerId, applyAt);
        if (availableTimes.isEmpty()) {
            throw new CustomException(ErrorCode.AVAILABLE_TIMES_IS_NOT_FOUND);
        }

        trainerService.deleteAvailableTimes(availableTimes);
    }
}
