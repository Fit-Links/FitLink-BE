package spring.fitlinkbe.interfaces.controller.trainer.dto;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AvailableTimesDto {

    @Builder
    public record Response(
            List<AvailableTimeResponse> currentSchedules,

            ScheduledChangeResponse scheduledChanges
    ) {
    }

    @Builder
    public record AvailableTimeResponse(
            Long availableTimeId,

            DayOfWeek dayOfWeek,

            Boolean isHoliday,

            LocalTime startTime,

            LocalTime endTime
    ) {
    }

    @Builder
    public record ScheduledChangeResponse(
            LocalDate applyAt,

            List<AvailableTimeResponse> schedules
    ) {
    }
}

