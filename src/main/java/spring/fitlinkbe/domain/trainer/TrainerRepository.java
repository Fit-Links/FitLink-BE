package spring.fitlinkbe.domain.trainer;

import java.util.Optional;

public interface TrainerRepository {

    Optional<Trainer> getTrainerInfo(Long trainerId);
}
