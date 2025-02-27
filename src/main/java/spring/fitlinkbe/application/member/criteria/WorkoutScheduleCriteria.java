package spring.fitlinkbe.application.member.criteria;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.WorkoutSchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class WorkoutScheduleCriteria {

    @Builder
    public record Request(
            Long workoutScheduleId,
            @NotNull DayOfWeek dayOfWeek,
            @NotNull List<LocalTime> preferenceTimes
    ) {
        public WorkoutSchedule toDomain(Member member) {
            return WorkoutSchedule.builder()
                    .member(member)
                    .dayOfWeek(dayOfWeek)
                    .preferenceTimes(preferenceTimes)
                    .build();
        }
    }
}
