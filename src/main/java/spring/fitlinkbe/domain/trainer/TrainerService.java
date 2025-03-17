package spring.fitlinkbe.domain.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.util.List;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.TRAINER_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final PersonalDetailRepository personalDetailRepository;

    @Transactional(readOnly = true)
    public Trainer getTrainerInfo(Long trainerId) {

        return trainerRepository.getTrainerInfo(trainerId)
                .orElseThrow(() -> new CustomException(TRAINER_IS_NOT_FOUND,
                        "트레이너 정보가 존재하지 않습니다. [trainerID: %d]".formatted(trainerId)));
    }

    public PersonalDetail registerTrainer(Long personalDetailId, AuthCommand.TrainerRegisterRequest command, Trainer savedTrainer) {
        PersonalDetail personalDetail = personalDetailRepository.getById(personalDetailId);
        personalDetail.registerTrainer(command.name(), command.birthDate(), command.phoneNumber(), command.profileUrl(), command.gender(), savedTrainer);
        personalDetailRepository.savePersonalDetail(personalDetail);

        return personalDetail;
    }

    public Trainer saveTrainer(Trainer trainer) {
        return trainerRepository.saveTrainer(trainer).orElseThrow();
    }

    public void saveAvailableTimes(List<AvailableTime> availableTimes) {
        trainerRepository.saveAvailableTimes(availableTimes);
    }

    public Trainer getTrainerByCode(String trainerCode) {
        return trainerRepository.getTrainerByCode(trainerCode);
    }

    public PersonalDetail getTrainerDetail(Long trainerId) {
        return personalDetailRepository.getTrainerDetail(trainerId)
                .orElseThrow(() -> new CustomException(TRAINER_IS_NOT_FOUND,
                        "트레이너 상세 정보를 찾을 수 없습니다. [trainerId: %d]".formatted(trainerId)));
    }

    public void savePersonalDetail(PersonalDetail personalDetail) {
        personalDetailRepository.savePersonalDetail(personalDetail);
    }
}
