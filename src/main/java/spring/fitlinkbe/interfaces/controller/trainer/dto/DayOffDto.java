package spring.fitlinkbe.interfaces.controller.trainer.dto;

import java.time.LocalDate;

public class DayOffDto {
    public record Response(Long dayOffId, LocalDate dayOffDate) {
    }
}
