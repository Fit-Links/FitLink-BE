package spring.fitlinkbe.infra.trainer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerJpaRepository extends JpaRepository<TrainerEntity, Long> {
}
