package spring.fitlinkbe.infra.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.member.WorkoutSchedule;
import spring.fitlinkbe.domain.member.WorkoutScheduleRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WorkoutScheduleRepositoryImpl implements WorkoutScheduleRepository {

    private final WorkoutScheduleJpaRepository workoutScheduleJpaRepository;

    @Override
    public List<WorkoutSchedule> saveAll(List<WorkoutSchedule> workoutSchedules) {
        List<WorkoutScheduleEntity> workoutScheduleEntities = workoutSchedules.stream()
                .map(WorkoutScheduleEntity::from).toList();

        return workoutScheduleJpaRepository.saveAll(workoutScheduleEntities)
                .stream().map(WorkoutScheduleEntity::toDomain).toList();
    }

    @Override
    public List<WorkoutSchedule> findAllByMemberId(Long memberId) {
        return workoutScheduleJpaRepository.findAllByMember_MemberId(memberId).stream()
                .map(WorkoutScheduleEntity::toDomain).toList();
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        workoutScheduleJpaRepository.deleteAllByWorkoutScheduleIdIn(ids);
    }
}
