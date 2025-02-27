package spring.fitlinkbe.domain.member;

import java.util.List;

public interface WorkoutScheduleRepository {
    List<WorkoutSchedule> saveAll(List<WorkoutSchedule> workoutSchedules);

    List<WorkoutSchedule> findAllByMemberId(Long memberId);

    void deleteAllByIds(List<Long> ids);
}
