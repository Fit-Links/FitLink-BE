package spring.fitlinkbe.interfaces.controller.member.dto;

import spring.fitlinkbe.domain.reservation.Session;

import java.time.LocalDateTime;

public class MemberSessionDto {
    public record SessionResponse(
            Long sessionId,
            Session.Status status,
            LocalDateTime date
    ) {
    }
}
