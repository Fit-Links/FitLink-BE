package spring.fitlinkbe.infra.trainer;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailableTimeJpaRepository extends JpaRepository<AvailableTimeEntity, Long> {

    @EntityGraph(attributePaths = {"trainer"})
    List<AvailableTimeEntity> findAllByTrainer_TrainerId(Long trainerId);
}
