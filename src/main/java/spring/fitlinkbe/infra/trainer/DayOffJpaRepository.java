package spring.fitlinkbe.infra.trainer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayOffJpaRepository extends JpaRepository<DayOffEntity, Long> {

    List<DayOffEntity> findAllByTrainer_TrainerId(Long trainerId);


}
