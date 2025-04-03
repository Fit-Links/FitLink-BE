package spring.fitlinkbe.application.trainer.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.trainer.AvailableTime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AvailableTimesResult {

    @Builder
    public record Response(
            ScheduledChangeResponse currentSchedules,

            ScheduledChangeResponse scheduledChanges
    ) {
        public static Response of(List<AvailableTime> currentSchedules, List<AvailableTime> scheduledSchedules) {
            return Response.builder()
                    .currentSchedules(ScheduledChangeResponse.from(currentSchedules))
                    .scheduledChanges(ScheduledChangeResponse.from(scheduledSchedules))
                    .build();
        }
    }

    @Builder
    public record AvailableTimeResponse(
            Long availableTimeId,

            DayOfWeek dayOfWeek,

            Boolean isHoliday,

            LocalTime startTime,

            LocalTime endTime
    ) {
        public static AvailableTimeResponse from(AvailableTime availableTime) {
            return AvailableTimeResponse.builder()
                    .availableTimeId(availableTime.getAvailableTimeId())
                    .dayOfWeek(availableTime.getDayOfWeek())
                    .isHoliday(availableTime.getIsHoliday())
                    .startTime(availableTime.getStartTime())
                    .endTime(availableTime.getEndTime())
                    .build();
        }
    }

    @Builder
    public record ScheduledChangeResponse(
            LocalDate applyAt,

            List<AvailableTimeResponse> schedules
    ) {
        public static ScheduledChangeResponse from(List<AvailableTime> scheduledSchedules) {
            if (scheduledSchedules.isEmpty()) return null;

            return ScheduledChangeResponse.builder()
                    .applyAt(scheduledSchedules.stream().findFirst().get().getApplyAt())
                    .schedules(scheduledSchedules.stream()
                            .map(AvailableTimeResponse::from)
                            .toList())
                    .build();
        }
    }
}

