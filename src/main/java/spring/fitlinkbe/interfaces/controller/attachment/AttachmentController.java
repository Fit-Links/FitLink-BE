package spring.fitlinkbe.interfaces.controller.attachment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.application.attachment.AttachmentFacade;
import spring.fitlinkbe.application.attachment.dto.PresignedUrlResult;
import spring.fitlinkbe.interfaces.controller.attachment.dto.AttachmentCreateRequest;
import spring.fitlinkbe.interfaces.controller.attachment.dto.PresignedUrlResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/attachments")
public class AttachmentController {

    private final AttachmentFacade attachmentFacade;

    @PostMapping("/pre-signed-url")
    public PresignedUrlResponse getPreSignedUrl(
            @RequestBody @Valid AttachmentCreateRequest requestBody
    ) {
        PresignedUrlResult preSignedUrlResult = attachmentFacade.getPreSignedUrl(requestBody.fileName(), requestBody.contentLength(), requestBody.contentType());

        return PresignedUrlResponse.from(preSignedUrlResult);
    }
}
