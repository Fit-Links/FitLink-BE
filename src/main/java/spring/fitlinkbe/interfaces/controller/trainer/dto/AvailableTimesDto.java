package spring.fitlinkbe.interfaces.controller.trainer.dto;

import lombok.Builder;
import spring.fitlinkbe.application.trainer.criteria.AvailableTimesResult;

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
        public static Response from(AvailableTimesResult.Response response) {
            return Response.builder()
                    .currentSchedules(response.currentSchedules().stream()
                            .map(AvailableTimeResponse::from)
                            .toList())
                    .scheduledChanges(ScheduledChangeResponse.from(response.scheduledChanges()))
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
        public static AvailableTimeResponse from(AvailableTimesResult.AvailableTimeResponse availableTimeResponse) {
            return AvailableTimeResponse.builder()
                    .availableTimeId(availableTimeResponse.availableTimeId())
                    .dayOfWeek(availableTimeResponse.dayOfWeek())
                    .isHoliday(availableTimeResponse.isHoliday())
                    .startTime(availableTimeResponse.startTime())
                    .endTime(availableTimeResponse.endTime())
                    .build();
        }
    }

    @Builder
    public record ScheduledChangeResponse(
            LocalDate applyAt,

            List<AvailableTimeResponse> schedules
    ) {
        public static ScheduledChangeResponse from(AvailableTimesResult.ScheduledChangeResponse scheduledChangeResponse) {
            if (scheduledChangeResponse == null) return null;

            return ScheduledChangeResponse.builder()
                    .applyAt(scheduledChangeResponse.applyAt())
                    .schedules(scheduledChangeResponse.schedules().stream()
                            .map(AvailableTimeResponse::from)
                            .toList())
                    .build();
        }
    }

    public record AddRequest(
            LocalDate applyAt,
            List<AvailableTimeRequest> scheduledChanges
    ) {

    }

    public record AvailableTimeRequest(

            DayOfWeek dayOfWeek,

            Boolean isHoliday,

            LocalTime startTime,

            LocalTime endTime
    ) {
    }
}

