package spring.fitlinkbe.application.member.criteria;

import spring.fitlinkbe.domain.reservation.Session;

import java.time.LocalDateTime;

public class MemberSessionResult {
    public record SessionResponse(
            Long sessionId,
            Session.Status status,
            LocalDateTime date
    ) {
        public static SessionResponse from(Session session) {
            return new SessionResponse(
                    session.getSessionId(),
                    session.getStatus(),
                    session.getReservation().getReservationDate()
            );
        }
    }
}
