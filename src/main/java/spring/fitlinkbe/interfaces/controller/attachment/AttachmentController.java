package spring.fitlinkbe.interfaces.controller.attachment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.application.attachment.AttachmentFacade;
import spring.fitlinkbe.application.attachment.dto.PresignedUrlResult;
import spring.fitlinkbe.interfaces.controller.attachment.dto.AttachmentDto;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/attachments")
public class AttachmentController {

    private final AttachmentFacade attachmentFacade;

    @PostMapping("/pre-signed-url")
    public ApiResultResponse<AttachmentDto.PresignedUrlResponseDto> getPreSignedUrl(
            @RequestBody @Valid AttachmentDto.AttachmentCreateDto requestBody
    ) {
        PresignedUrlResult preSignedUrlResult = attachmentFacade.getPreSignedUrl(requestBody.fileName(), requestBody.contentLength(), requestBody.contentType());

        return ApiResultResponse.ok(AttachmentDto.PresignedUrlResponseDto.from(preSignedUrlResult));
    }

    @PostMapping("/user-profile")
    public ApiResultResponse<Object> updateProfile(
            @Login SecurityUser user,
            @RequestBody @Valid AttachmentDto.UserAttachmentAddDto requestBody
    ) {
        attachmentFacade.updateProfile(user.getPersonalDetailId(), requestBody.attachmentId());

        return ApiResultResponse.of(HttpStatus.CREATED, true, null);
    }

}
