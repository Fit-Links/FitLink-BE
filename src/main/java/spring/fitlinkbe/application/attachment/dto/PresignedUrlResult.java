package spring.fitlinkbe.application.attachment.dto;

import lombok.Builder;

@Builder
public record PresignedUrlResult(
        String presignedUrl,
        Long attachmentId
) {
}
