package spring.fitlinkbe.application.trainer.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.trainer.DayOff;

import java.time.LocalDate;

public class DayOffResult {

    @Builder
    public record Response(Long dayOffId, LocalDate dayOffDate) {
        public static Response from(DayOff dayOff) {
            return Response.builder()
                    .dayOffId(dayOff.getDayOffId())
                    .dayOffDate(dayOff.getDayOffDate())
                    .build();
        }
    }
}
