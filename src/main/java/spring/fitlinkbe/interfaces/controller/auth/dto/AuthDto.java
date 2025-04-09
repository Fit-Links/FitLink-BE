package spring.fitlinkbe.interfaces.controller.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import spring.fitlinkbe.domain.auth.command.AuthCommand;
import spring.fitlinkbe.domain.common.model.PersonalDetail.Gender;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AuthDto {

    @Builder
    public record EmailAuthTokenResponse(
            String verificationToken
    ) {
    }


    @Builder
    public record Response(String accessToken, String refreshToken) {

        public static Response from(AuthCommand.Response so) {
            return Response.builder()
                    .accessToken(so.accessToken())
                    .refreshToken(so.refreshToken())
                    .build();
        }

    }

    public record TrainerRegisterRequest(
            @NotNull String name,
            @NotNull LocalDate birthDate,
            @NotNull Gender gender,
            String profileUrl,
            @Valid List<AvailableTimeRequest> availableTimes
    ) {
        public AuthCommand.TrainerRegisterRequest toCommand() {
            return AuthCommand.TrainerRegisterRequest.builder()
                    .name(name)
                    .birthDate(birthDate)
                    .gender(gender)
                    .profileUrl(profileUrl)
                    .availableTimes(availableTimes.stream().map(AvailableTimeRequest::toCommand).toList())
                    .build();
        }

        @JsonIgnore
        @AssertTrue(message = "수업 가능 시간의 요일은 겹치면 안됩니다.")
        private boolean isAvailableTimeDayOfWeekUnique() {
            if (availableTimes == null) {
                return true;
            }
            return availableTimes.stream()
                    .map(AvailableTimeRequest::dayOfWeek)
                    .distinct()
                    .count() == availableTimes.size();
        }
    }

    public record AvailableTimeRequest(
            @NotNull DayOfWeek dayOfWeek,
            @NotNull Boolean isHoliday,
            LocalTime startTime,
            LocalTime endTime
    ) {
        public AuthCommand.AvailableTimeRequest toCommand() {
            return AuthCommand.AvailableTimeRequest.builder()
                    .dayOfWeek(dayOfWeek)
                    .isHoliday(isHoliday)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
        }

        @JsonIgnore
        @AssertTrue(message = "시작 시간과 종료 시간은 둘 다 선택하던지 둘 다 선택하지 않아야 합니다.")
        private boolean isTimeValid() {
            return (startTime == null && endTime == null) || (startTime != null && endTime != null);
        }

        @JsonIgnore
        @AssertTrue(message = "시작 시간은 종료 시간보다 빨라야 합니다.")
        private boolean isStartTimeBeforeEndTime() {
            if (startTime == null || endTime == null) {
                return true;
            }
            return startTime.isBefore(endTime);
        }

        @JsonIgnore
        @AssertTrue(message = "휴일인 경우에만 시작 시간과 종료 시간은 선택 입니다.")
        private boolean isHolidayTimeValid() {
            if (isHoliday == null) {
                return true;
            }
            return isHoliday || (startTime != null && endTime != null);
        }
    }


    public record MemberRegisterRequest(
            @NotNull String name,
            @NotNull LocalDate birthDate,
            @NotNull Gender gender,
            String profileUrl,
            @Valid List<WorkoutScheduleRequest> workoutSchedule
    ) {

        public AuthCommand.MemberRegisterRequest toCommand() {
            return AuthCommand.MemberRegisterRequest.builder()
                    .name(name)
                    .birthDate(birthDate)
                    .gender(gender)
                    .profileUrl(profileUrl)
                    .workoutSchedule(workoutSchedule.stream().map(WorkoutScheduleRequest::toCommand).toList())
                    .build();
        }

        @JsonIgnore
        @AssertTrue(message = "운동 희망일의 요일은 겹치면 안됩니다.")
        private boolean isWorkoutScheduleDayOfWeekUnique() {
            if (workoutSchedule == null) {
                return true;
            }
            return workoutSchedule.stream()
                    .map(WorkoutScheduleRequest::dayOfWeek)
                    .distinct()
                    .count() == workoutSchedule.size();
        }
    }

    public record WorkoutScheduleRequest(@NotNull DayOfWeek dayOfWeek, @NotNull List<LocalTime> preferenceTimes) {

        public AuthCommand.WorkoutScheduleRequest toCommand() {
            return AuthCommand.WorkoutScheduleRequest.builder()
                    .dayOfWeek(dayOfWeek)
                    .preferenceTimes(new ArrayList<>(preferenceTimes))
                    .build();
        }
    }
}
