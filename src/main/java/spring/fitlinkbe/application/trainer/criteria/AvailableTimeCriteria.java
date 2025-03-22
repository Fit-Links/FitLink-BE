package spring.fitlinkbe.application.trainer.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AvailableTimeCriteria {


    @Builder
    public record AddRequest(
            LocalDate applyAt,
            List<AvailableTimeRequest> availableTimes
    ) {

        public List<AvailableTime> toAvailableTimes(Trainer trainer) {
            List<AvailableTime> result = new ArrayList<>();
            for (AvailableTimeRequest availableTime : availableTimes) {
                result.add(AvailableTime.builder()
                        .trainer(trainer)
                        .dayOfWeek(availableTime.dayOfWeek())
                        .applyAt(applyAt)
                        .isHoliday(availableTime.isHoliday())
                        .startTime(availableTime.startTime())
                        .endTime(availableTime.endTime())
                        .build());
            }

            return result;
        }
    }

    @Builder
    public record AvailableTimeRequest(

            DayOfWeek dayOfWeek,

            Boolean isHoliday,

            LocalTime startTime,

            LocalTime endTime
    ) {
    }
}
