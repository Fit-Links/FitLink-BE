package spring.fitlinkbe.domain.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.exception.CustomException;

import static spring.fitlinkbe.domain.common.exception.ErrorCode.TRAINER_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;

    @Transactional(readOnly = true)
    public Trainer getTrainerInfo(Long trainerId) {

        return trainerRepository.getTrainerInfo(trainerId)
                .orElseThrow(() -> new CustomException(TRAINER_IS_NOT_FOUND,
                        "트레이너 정보가 존재하지 않습니다. [trainerID: %d]".formatted(trainerId)));
    }
}
