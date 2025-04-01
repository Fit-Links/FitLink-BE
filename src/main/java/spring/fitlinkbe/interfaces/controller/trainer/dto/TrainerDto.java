package spring.fitlinkbe.interfaces.controller.trainer.dto;

public class TrainerDto {

    public record MemberDisconnectRequest(
            Long memberId
    ) {
    }
}

