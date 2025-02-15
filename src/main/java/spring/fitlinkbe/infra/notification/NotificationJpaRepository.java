package spring.fitlinkbe.infra.notification;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    @EntityGraph(attributePaths = {"personalDetail"})
    Optional<NotificationEntity> findByPersonalDetail_PersonalDetailId(Long personalDetailId);
}
