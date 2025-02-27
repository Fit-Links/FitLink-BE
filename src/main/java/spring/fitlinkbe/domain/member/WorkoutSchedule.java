package spring.fitlinkbe.domain.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class WorkoutSchedule {

    private Long workoutScheduleId;

    private DayOfWeek dayOfWeek;

    private Member member;

    private List<LocalTime> preferenceTimes;

    public Long getMemberId() {
        return member.getMemberId();
    }

    public void update(DayOfWeek dayOfWeek, List<LocalTime> localTimes) {
        this.dayOfWeek = dayOfWeek;
        this.preferenceTimes = localTimes;
    }
}
