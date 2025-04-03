package spring.fitlinkbe.domain.trainer;

import java.time.LocalDate;
import java.util.List;

public interface AvailableTimeRepository {
    LocalDate getCurrentAppliedDate(Long trainerId);

    LocalDate getScheduledAppliedDate(Long trainerId);

    List<AvailableTime> getAvailableTimes(Long trainerId, LocalDate appliedDate);

    void deleteAll(List<AvailableTime> availableTimes);
}
