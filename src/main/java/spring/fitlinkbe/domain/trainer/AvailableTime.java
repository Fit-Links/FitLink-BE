package spring.fitlinkbe.domain.trainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTime {
    private Long availableTimeId;

    private Trainer trainer;

    private DayOfWeek dayOfWeek;

    private Boolean isHoliday;

    private Boolean unavailable;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
