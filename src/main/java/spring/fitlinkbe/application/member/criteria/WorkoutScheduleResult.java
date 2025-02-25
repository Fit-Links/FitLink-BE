package spring.fitlinkbe.application.member.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.member.WorkoutSchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class WorkoutScheduleResult {

    @Builder
    public record Response(
            Long workoutScheduleId,
            DayOfWeek dayOfWeek,
            List<LocalTime> preferenceTimes
    ) {
        public static Response from(WorkoutSchedule workoutSchedule) {
            return Response.builder()
                    .workoutScheduleId(workoutSchedule.getWorkoutScheduleId())
                    .dayOfWeek(workoutSchedule.getDayOfWeek())
                    .preferenceTimes(workoutSchedule.getPreferenceTimes())
                    .build();
        }
    }
}

