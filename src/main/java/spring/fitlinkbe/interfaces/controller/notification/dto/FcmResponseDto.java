package spring.fitlinkbe.interfaces.controller.notification.dto;

import lombok.Builder;

public class FcmResponseDto {

    @Builder(toBuilder = true)
    public record FcmTokenResponse(String message) {
        public static FcmTokenResponse of(String message) {
            return FcmTokenResponse.builder()
                    .message(message)
                    .build();
        }
    }
}
