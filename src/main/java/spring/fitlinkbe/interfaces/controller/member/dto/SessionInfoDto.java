package spring.fitlinkbe.interfaces.controller.member.dto;

import jakarta.validation.constraints.PositiveOrZero;

public class SessionInfoDto {

    public record UpdateRequest(

            @PositiveOrZero
            Integer remainingCount,

            @PositiveOrZero
            Integer totalCount
    ) {
    }

    public record Response(
            Long sessionInfoId,
            Integer remainingCount,
            Integer totalCount
    ) {
    }
}
