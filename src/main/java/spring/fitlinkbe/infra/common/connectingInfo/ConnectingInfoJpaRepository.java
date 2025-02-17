package spring.fitlinkbe.infra.common.connectingInfo;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConnectingInfoJpaRepository extends JpaRepository<ConnectingInfoEntity, Long> {

    @EntityGraph(attributePaths = {"member", "trainer"})
    Optional<ConnectingInfoEntity> findByMember_MemberIdAndTrainer_TrainerId(Long memberId, Long trainerId);

    /**
     * 해당 회원의 이미 존재하는 REJECTED 되지 않은 ConnectingInfoEntity 를 조회한다.
     *
     * @param memberId
     * @return
     */
    @Query("select c from ConnectingInfoEntity c join fetch c.member m where m.memberId = :memberId and c.status != 'REJECTED'")
    Optional<ConnectingInfoEntity> findExistMemberConnectingInfo(Long memberId);
}
