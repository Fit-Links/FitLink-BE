package spring.fitlinkbe.infra.trainer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DayOffJpaRepository extends JpaRepository<DayOffEntity, Long> {

    List<DayOffEntity> findAllByTrainer_TrainerId(Long trainerId);

    boolean existsByTrainer_TrainerIdAndDayOffDateIn(Long trainerId, List<LocalDate> dayOffDates);

    Optional<DayOffEntity> findByTrainer_TrainerIdAndDayOffId(Long trainerId, Long dayOffId);


    @Query("SELECT d FROM DayOffEntity d WHERE d.trainer.trainerId = :trainerId AND d.dayOffDate >= CURRENT_DATE")
    List<DayOffEntity> findScheduledDayOff(Long trainerId);
}
