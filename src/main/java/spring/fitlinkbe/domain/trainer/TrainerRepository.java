package spring.fitlinkbe.domain.trainer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerRepository {

    Optional<Trainer> getTrainerInfo(Long trainerId);

    List<AvailableTime> getTrainerAvailableTimes(Long trainerId);

    List<Trainer> getTrainers();

    List<DayOff> getTrainerDayOffs(Long trainerId);

    Optional<Trainer> saveTrainer(Trainer trainer);

    Optional<DayOff> saveDayOff(DayOff dayOff);

    void saveAvailableTimes(List<AvailableTime> availableTimes);

    AvailableTime saveAvailableTime(AvailableTime availableTime);

    Trainer getTrainerByCode(String trainerCode);

    boolean isDayOffExists(Long trainerId, List<LocalDate> dayOffDates);

    List<DayOff> saveAllDayOffs(List<DayOff> dayOffs);

    Optional<DayOff> findDayOff(Long trainerId, Long dayOffId);

    void deleteDayOff(DayOff dayOff);

    List<DayOff> findScheduledDayOff(Long trainerId);
}
