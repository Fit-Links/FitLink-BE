package spring.fitlinkbe.interfaces.controller.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import spring.fitlinkbe.application.member.criteria.WorkoutScheduleResult;
import spring.fitlinkbe.domain.auth.command.AuthCommand;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
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
    public record Request(
            Long workoutScheduleId,
            @NotNull DayOfWeek dayOfWeek,
            @NotNull List<LocalTime> preferenceTimes
    ) {
    }
}

