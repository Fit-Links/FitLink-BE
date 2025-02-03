package spring.fitlinkbe.infra.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.trainer.DayOff;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainerRepositoryImpl implements TrainerRepository {

    private final TrainerJpaRepository trainerJpaRepository;
    private final DayOffJpaRepository dayOffJpaRepository;

    @Override
    public Optional<Trainer> getTrainerInfo(Long trainerId) {
        Optional<TrainerEntity> trainerEntity = trainerJpaRepository.findById(trainerId);

        if (trainerEntity.isPresent()) {
            return trainerEntity.map(TrainerEntity::toDomain);
        }

        return Optional.empty();
    }

    @Override
    public List<Trainer> getTrainers() {

        return trainerJpaRepository.findAll()
                .stream().map(TrainerEntity::toDomain)
                .toList();
    }

    @Override
    public List<DayOff> getTrainerDayOffs(Long trainerId) {

        return dayOffJpaRepository.findAllByTrainer_TrainerId(trainerId)
                .stream()
                .map(DayOffEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Trainer> saveTrainer(Trainer trainer) {
        TrainerEntity savedEntity = trainerJpaRepository.save(TrainerEntity.from(trainer));

        return Optional.of(savedEntity.toDomain());
    }

    @Override
    public Optional<DayOff> saveDayOff(DayOff dayOff) {
        DayOffEntity savedEntity = dayOffJpaRepository.save(DayOffEntity.from(dayOff));

        return Optional.of(savedEntity.toDomain());
    }
}
