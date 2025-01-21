package spring.fitlinkbe.infra.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainerRepositoryImpl implements TrainerRepository {

    private final TrainerJpaRepository trainerJpaRepository;

    @Override
    public Optional<Trainer> getTrainerInfo(Long trainerId) {
        Optional<TrainerEntity> trainerEntity = trainerJpaRepository.findById(trainerId);

        if (trainerEntity.isPresent()) {
            return trainerEntity.map(TrainerEntity::toDomain);
        }

        return Optional.empty();
    }
}
