package spring.fitlinkbe.interfaces.controller.member.dto;

import lombok.Builder;
import spring.fitlinkbe.application.member.criteria.MemberSessionResult;
import spring.fitlinkbe.domain.reservation.Session;

import java.time.LocalDateTime;

public class MemberSessionDto {

    @Builder
    public record SessionResponse(
            Long sessionId,
            Session.Status status,
            LocalDateTime date
    ) {
        public static SessionResponse from(MemberSessionResult.SessionResponse result) {
            return SessionResponse.builder()
                    .sessionId(result.sessionId())
                    .status(result.status())
                    .date(result.date())
                    .build();
        }
    }
}
