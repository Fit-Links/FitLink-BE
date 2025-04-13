package spring.fitlinkbe.interfaces.controller.attachment.dto;

import lombok.Builder;
import spring.fitlinkbe.application.attachment.dto.PresignedUrlResult;

@Builder
public record PresignedUrlResponse(
        String presignedUrl,
        Long attachmentId
) {
    public static PresignedUrlResponse from(PresignedUrlResult preSignedUrlResult) {
        return PresignedUrlResponse.builder()
                .presignedUrl(preSignedUrlResult.presignedUrl())
                .attachmentId(preSignedUrlResult.attachmentId())
                .build();
    }
}
