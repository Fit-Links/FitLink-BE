package spring.fitlinkbe.interfaces.controller.member.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import spring.fitlinkbe.application.member.criteria.WorkoutScheduleCriteria;
import spring.fitlinkbe.application.member.criteria.WorkoutScheduleResult;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class WorkoutScheduleDto {

    @Builder
    public record Response(
            Long workoutScheduleId,
            DayOfWeek dayOfWeek,
            List<LocalTime> preferenceTimes
    ) {
        public static Response from(WorkoutScheduleResult.Response result) {
            return Response.builder()
                    .workoutScheduleId(result.workoutScheduleId())
                    .dayOfWeek(result.dayOfWeek())
                    .preferenceTimes(result.preferenceTimes())
                    .build();
        }
    }

    @Builder
    @Valid
    public record Request(
            Long workoutScheduleId,
            @NotNull DayOfWeek dayOfWeek,
            @NotNull List<LocalTime> preferenceTimes
    ) {
        public WorkoutScheduleCriteria.Request toCriteria() {
            return WorkoutScheduleCriteria.Request.builder()
                    .workoutScheduleId(workoutScheduleId)
                    .dayOfWeek(dayOfWeek)
                    .preferenceTimes(preferenceTimes)
                    .build();
        }
    }
}

