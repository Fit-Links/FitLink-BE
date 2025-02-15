package spring.fitlinkbe.infra.common.connectingInfo;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConnectingInfoJpaRepository extends JpaRepository<ConnectingInfoEntity, Long> {

    @EntityGraph(attributePaths = {"member", "trainer"})
    Optional<ConnectingInfoEntity> findByMember_MemberIdAndTrainer_TrainerId(Long memberId, Long trainerId);
}
