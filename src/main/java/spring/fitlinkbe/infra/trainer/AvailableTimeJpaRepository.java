package spring.fitlinkbe.infra.trainer;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AvailableTimeJpaRepository extends JpaRepository<AvailableTimeEntity, Long> {

    @EntityGraph(attributePaths = {"trainer"})
    List<AvailableTimeEntity> findAllByTrainer_TrainerId(Long trainerId);

    @Query("SELECT MAX(at.applyAt) FROM AvailableTimeEntity at " +
            "WHERE at.trainer.trainerId = :trainerId " +
            "AND at.applyAt <= CURRENT_TIMESTAMP")
    LocalDate getCurrentAppliedDate(Long trainerId);

    @Query("SELECT MIN(at.applyAt) FROM AvailableTimeEntity at " +
            "WHERE at.trainer.trainerId = :trainerId " +
            "AND at.applyAt > CURRENT_TIMESTAMP")
    LocalDate getScheduledAppliedDate(Long trainerId);

    @Query("SELECT at FROM AvailableTimeEntity at " +
            "JOIN FETCH at.trainer " +
            "WHERE at.trainer.trainerId = :trainerId " +
            "AND at.applyAt = :appliedDate")
    List<AvailableTimeEntity> findAllAvailableTimes(Long trainerId, LocalDate appliedDate);
}
