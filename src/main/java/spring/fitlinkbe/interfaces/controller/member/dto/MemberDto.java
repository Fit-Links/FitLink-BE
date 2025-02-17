package spring.fitlinkbe.interfaces.controller.member.dto;

import jakarta.validation.constraints.NotNull;

public class MemberDto {

    public record MemberConnectRequest(
            @NotNull String trainerCode
    ) {
    }
}
