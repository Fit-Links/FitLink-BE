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
    public void saveAll(List<WorkoutSchedule> workoutSchedules) {
        List<WorkoutScheduleEntity> workoutScheduleEntities = workoutSchedules.stream()
                .map(WorkoutScheduleEntity::from).toList();

        workoutScheduleJpaRepository.saveAll(workoutScheduleEntities);
    }
}
