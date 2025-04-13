package spring.fitlinkbe.interfaces.controller.attachment.dto;


import jakarta.validation.constraints.NotNull;

public record AttachmentCreateRequest(

        @NotNull
        String fileName,
        @NotNull
        String contentLength,
        @NotNull
        String contentType
) {
}
