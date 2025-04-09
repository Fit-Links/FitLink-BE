package spring.fitlinkbe.interfaces.controller.trainer.dto;

import jakarta.validation.constraints.NotNull;

public class TrainerDto {

    public record MemberDisconnectRequest(

            @NotNull
            Long memberId
    ) {
    }
}

