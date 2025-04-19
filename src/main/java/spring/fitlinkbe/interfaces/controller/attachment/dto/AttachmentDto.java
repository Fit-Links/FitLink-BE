package spring.fitlinkbe.interfaces.controller.attachment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import spring.fitlinkbe.application.attachment.dto.PresignedUrlResult;

public class AttachmentDto {
    public record AttachmentCreateDto(

            @NotNull
            String fileName,
            @NotNull
            String contentLength,
            @NotNull
            String contentType
    ) {
    }

    @Builder
    public record PresignedUrlResponseDto(
            String presignedUrl,
            Long attachmentId
    ) {
        public static PresignedUrlResponseDto from(PresignedUrlResult preSignedUrlResult) {
            return PresignedUrlResponseDto.builder()
                    .presignedUrl(preSignedUrlResult.presignedUrl())
                    .attachmentId(preSignedUrlResult.attachmentId())
                    .build();
        }
    }

    public record UserAttachmentAddDto(

            @NotNull
            Long attachmentId
    ) {
    }
}
