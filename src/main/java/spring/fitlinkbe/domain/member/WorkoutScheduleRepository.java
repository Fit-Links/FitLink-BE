package spring.fitlinkbe.domain.member;

import java.util.List;

public interface WorkoutScheduleRepository {
    void saveAll(List<WorkoutSchedule> workoutSchedules);

    List<WorkoutSchedule> findAllByMemberId(Long memberId);
}
