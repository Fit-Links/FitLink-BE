package spring.fitlinkbe.infra.member;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutScheduleJpaRepository extends JpaRepository<WorkoutScheduleEntity, Long> {

    @EntityGraph(attributePaths = {"member"})
    List<WorkoutScheduleEntity> findAllByMember_MemberId(Long memberId);
}
