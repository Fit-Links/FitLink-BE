package spring.fitlinkbe.domain.trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository {

    Optional<Trainer> getTrainerInfo(Long trainerId);

    List<Trainer> getTrainers();

    List<DayOff> getTrainerDayOffs(Long trainerId);

    Optional<Trainer> saveTrainer(Trainer trainer);

    Optional<DayOff> saveDayOff(DayOff dayOff);

}
