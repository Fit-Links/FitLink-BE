package spring.fitlinkbe.domain.trainer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DayOff {

    private Long dayOffId;
    private Trainer trainer;
    private LocalDate dayOffDate;
}
