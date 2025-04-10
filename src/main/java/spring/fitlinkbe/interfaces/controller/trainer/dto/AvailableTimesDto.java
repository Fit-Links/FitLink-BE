package spring.fitlinkbe.interfaces.controller.trainer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import spring.fitlinkbe.application.trainer.criteria.AvailableTimeCriteria;
import spring.fitlinkbe.application.trainer.criteria.AvailableTimesResult;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AvailableTimesDto {

    @Builder
    public record Response(
            ScheduledChangeResponse currentSchedules,

            ScheduledChangeResponse scheduledChanges
    ) {
        public static Response from(AvailableTimesResult.Response response) {
            return Response.builder()
                    .currentSchedules(ScheduledChangeResponse.from(response.currentSchedules()))
                    .scheduledChanges(ScheduledChangeResponse.from(response.scheduledChanges()))
                    .build();
        }
    }

    @Builder
    public record CurrentAvailableTimesResponse(
            ScheduledChangeResponse currentSchedules
    ){
        public static CurrentAvailableTimesResponse from(AvailableTimesResult.CurrentAvailableTimesResponse response) {
            return CurrentAvailableTimesResponse.builder()
                    .currentSchedules(ScheduledChangeResponse.from(response.currentSchedules()))
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
            @NotNull LocalDate applyAt,
            @Valid List<AvailableTimeRequest> availableTimes
    ) {

        public AvailableTimeCriteria.AddRequest toCriteria() {
            return AvailableTimeCriteria.AddRequest.builder()
                    .applyAt(applyAt)
                    .availableTimes(availableTimes.stream()
                            .map(AvailableTimeRequest::toCriteria)
                            .toList())
                    .build();
        }

        @AssertTrue(message = "스케쥴 리스트는 비어있어선 안됩니다.")
        public boolean isNotEmpty() {
            return availableTimes != null && !availableTimes.isEmpty();
        }

        @AssertTrue(message = "요일은 겹칠 수 없습니다.")
        public boolean isNotOverlap() {
            return availableTimes == null || availableTimes.stream()
                    .map(AvailableTimeRequest::dayOfWeek)
                    .distinct()
                    .count() == availableTimes.size();
        }

        @AssertTrue(message = "적용 날짜는 오늘 이후여야 합니다.")
        public boolean isFutureApplyAt() {
            return applyAt == null || applyAt.isAfter(LocalDate.now()) || applyAt.isEqual(LocalDate.now());
        }

        @AssertTrue(message = "시작 시간은 종료 시간보다 빨라야 합니다.")
        public boolean isStartTimeBeforeEndTime() {
            return availableTimes == null || availableTimes.stream()
                    .allMatch(scheduledChange -> scheduledChange.startTime().isBefore(scheduledChange.endTime()));
        }
    }


    public record AvailableTimeRequest(

            DayOfWeek dayOfWeek,

            Boolean isHoliday,

            @NotNull LocalTime startTime,

            @NotNull LocalTime endTime
    ) {
        public AvailableTimeCriteria.AvailableTimeRequest toCriteria() {
            return AvailableTimeCriteria.AvailableTimeRequest.builder()
                    .dayOfWeek(dayOfWeek)
                    .isHoliday(isHoliday)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
        }
    }
}

