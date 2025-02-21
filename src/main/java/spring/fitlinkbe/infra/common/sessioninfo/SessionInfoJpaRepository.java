package spring.fitlinkbe.infra.common.sessioninfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SessionInfoJpaRepository extends JpaRepository<SessionInfoEntity, Long> {

    @Query("SELECT si FROM SessionInfoEntity si " +
            "LEFT JOIN FETCH si.trainer " +
            "LEFT JOIN FETCH si.member " +
            "WHERE si.sessionInfoId = :sessionInfoId")
    Optional<SessionInfoEntity> findByIdJoinFetch(Long sessionInfoId);

    @Query("SELECT si FROM SessionInfoEntity si " +
            "LEFT JOIN FETCH si.trainer " +
            "LEFT JOIN FETCH si.member " +
            "WHERE si.member.memberId = :memberId")
    Optional<SessionInfoEntity> findByMemberIdJoinFetch(Long memberId);
}
