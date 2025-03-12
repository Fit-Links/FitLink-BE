package spring.fitlinkbe.infra.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long>, MemberRepositoryCustom {

    @Query("SELECT m FROM MemberEntity m " +
            "LEFT JOIN FETCH m.trainer " +
            "WHERE m.memberId = :memberId")
    Optional<MemberEntity> findByIdJoinFetch(Long memberId);
}
