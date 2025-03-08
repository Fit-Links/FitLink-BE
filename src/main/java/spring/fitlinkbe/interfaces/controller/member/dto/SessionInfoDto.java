package spring.fitlinkbe.interfaces.controller.member.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import spring.fitlinkbe.application.member.criteria.SessionInfoCriteria;

public class SessionInfoDto {

    public record UpdateRequest(

            @PositiveOrZero
            Integer remainingCount,

            @PositiveOrZero
            Integer totalCount
    ) {
        public SessionInfoCriteria.UpdateRequest toCriteria() {
            return SessionInfoCriteria.UpdateRequest.builder()
                    .remainingCount(remainingCount)
                    .totalCount(totalCount)
                    .build();
        }
    }

    @Builder
    public record Response(
            Long sessionInfoId,
            Integer remainingCount,
            Integer totalCount
    ) {
        public static Response from(SessionInfoCriteria.Response response) {
            return Response.builder()
                    .sessionInfoId(response.sessionInfoId())
                    .remainingCount(response.remainingCount())
                    .totalCount(response.totalCount())
                    .build();
        }
    }
}
