package spring.fitlinkbe.interfaces.controller.trainer.dto;

import lombok.Builder;

import java.time.LocalDate;

public class TrainerInfoDto {

    @Builder
    public record Response(
            Long trainerId,
            String name,
            LocalDate birthDate,
            String phoneNumber,
            String profilePictureUrl
    ) {
    }
}
