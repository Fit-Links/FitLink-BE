package spring.fitlinkbe.interfaces.controller.member.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class WorkoutScheduleDto {

    public record Response(
            Long workoutScheduleId,
            DayOfWeek dayOfWeek,
            List<LocalTime> preferenceTimes
    ) {
    }
}

