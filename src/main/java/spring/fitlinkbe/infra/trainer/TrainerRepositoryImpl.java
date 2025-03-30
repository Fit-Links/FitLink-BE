package spring.fitlinkbe.infra.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.domain.trainer.DayOff;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainerRepositoryImpl implements TrainerRepository {

    private final TrainerJpaRepository trainerJpaRepository;
    private final DayOffJpaRepository dayOffJpaRepository;
    private final AvailableTimeJpaRepository availableTimeJpaRepository;

    @Override
    public Optional<Trainer> getTrainerInfo(Long trainerId) {
        Optional<TrainerEntity> trainerEntity = trainerJpaRepository.findById(trainerId);

        if (trainerEntity.isPresent()) {
            return trainerEntity.map(TrainerEntity::toDomain);
        }

        return Optional.empty();
    }

    @Override
    public List<AvailableTime> getTrainerAvailableTimes(Long trainerId) {
        return availableTimeJpaRepository.findAllByTrainer_TrainerId(trainerId)
                .stream()
                .map(AvailableTimeEntity::toDomain)
                .toList();
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

    @Override
    public void saveAvailableTimes(List<AvailableTime> availableTimes) {
        availableTimeJpaRepository.saveAll(availableTimes.stream()
                .map(AvailableTimeEntity::from)
                .toList());
    }

    @Override
    public AvailableTime saveAvailableTime(AvailableTime availableTime) {
        AvailableTimeEntity savedEntity =
                availableTimeJpaRepository.save(AvailableTimeEntity.from(availableTime));

        return savedEntity.toDomain();
    }

    @Override
    public Trainer getTrainerByCode(String trainerCode) {
        return trainerJpaRepository.findByTrainerCode(trainerCode)
                .orElseThrow(() -> new CustomException(ErrorCode.TRAINER_IS_NOT_FOUND)).toDomain();
    }

    @Override
    public boolean isDayOffExists(Long trainerId, List<LocalDate> dayOffDates) {
        return dayOffJpaRepository.existsByTrainer_TrainerIdAndDayOffDateIn(trainerId, dayOffDates);
    }

    @Override
    public List<DayOff> saveAllDayOffs(List<DayOff> dayOffs) {
        List<DayOffEntity> entities = dayOffs.stream()
                .map(DayOffEntity::from)
                .toList();

        List<DayOffEntity> savedEntities = dayOffJpaRepository.saveAll(entities);

        return savedEntities.stream()
                .map(DayOffEntity::toDomain)
                .toList();
    }
}
