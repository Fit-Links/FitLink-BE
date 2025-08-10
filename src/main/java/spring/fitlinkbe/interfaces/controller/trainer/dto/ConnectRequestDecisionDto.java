package spring.fitlinkbe.interfaces.controller.trainer.dto;

import jakarta.validation.constraints.NotNull;
import spring.fitlinkbe.application.trainer.criteria.ConnectRequestDecisionResult;

public class ConnectRequestDecisionDto {

    public record Request(
            @NotNull
            Boolean isApproved
    ) {
    }

    public record Response(
            Long memberId,
            Long sessionInfoId
    ) {
        public static Response from(ConnectRequestDecisionResult result) {
            return new Response(result.memberId(), result.sessionInfoId());
        }
    }
}
