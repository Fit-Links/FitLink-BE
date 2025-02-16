package spring.fitlinkbe.infra.trainer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerJpaRepository extends JpaRepository<TrainerEntity, Long> {

    Optional<TrainerEntity> findByTrainerCode(String trainerCode);
}
