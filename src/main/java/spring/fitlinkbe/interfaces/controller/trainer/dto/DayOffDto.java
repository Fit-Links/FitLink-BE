package spring.fitlinkbe.interfaces.controller.trainer.dto;

import lombok.Builder;
import spring.fitlinkbe.application.trainer.criteria.DayOffResult;

import java.time.LocalDate;

public class DayOffDto {

    @Builder
    public record Response(Long dayOffId, LocalDate dayOffDate) {
        public static Response from(DayOffResult.Response response) {
            return Response.builder()
                    .dayOffId(response.dayOffId())
                    .dayOffDate(response.dayOffDate())
                    .build();
        }
    }
}
