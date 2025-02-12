package spring.fitlinkbe.infra.trainer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailableTimeJpaRepository extends JpaRepository<AvailableTimeEntity, Long> {
    List<AvailableTimeEntity> findAllByTrainer_TrainerId(Long trainerId);
}
