package spring.fitlinkbe.application.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import spring.fitlinkbe.application.attachment.dto.PresignedUrlResult;
import spring.fitlinkbe.domain.attachment.AttachmentService;
import spring.fitlinkbe.domain.attachment.FileUploadService;
import spring.fitlinkbe.domain.attachment.model.Attachment;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class AttachmentFacade {

    private final AttachmentService attachmentService;
    private final FileUploadService fileUploadService;

    public PresignedUrlResult getPreSignedUrl(String fileName, String contentLength, String contentType) {
        String uuid = UUID.randomUUID().toString();
        String uploadFileName = uuid + "." + extractFileExtension(fileName);

        String presignedUrl = fileUploadService.getPresignedUrl(uploadFileName, contentLength, contentType);
        String uploadUrl = fileUploadService.getUploadUrl(uploadFileName);
        Attachment attachment = attachmentService.saveAttachment(Attachment.builder()
                .origFileName(fileName)
                .uuid(uuid)
                .uploadFilePath(uploadUrl)
                .fileSize(Long.parseLong(contentLength))
                .fileExtension(extractFileExtension(fileName))
                .build());

        return PresignedUrlResult.builder()
                .presignedUrl(presignedUrl)
                .attachmentId(attachment.getAttachmentId())
                .build();
    }

    private String extractFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
