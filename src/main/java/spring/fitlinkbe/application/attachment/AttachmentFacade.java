package spring.fitlinkbe.application.attachment;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.application.attachment.dto.PresignedUrlResult;
import spring.fitlinkbe.domain.attachment.AttachmentService;
import spring.fitlinkbe.domain.attachment.FileUploadService;
import spring.fitlinkbe.domain.attachment.model.Attachment;
import spring.fitlinkbe.domain.auth.AuthService;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberService;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class AttachmentFacade {

    private final AttachmentService attachmentService;
    private final FileUploadService fileUploadService;
    private final MemberService memberService;
    private final AuthService authService;

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

    @Transactional
    public void updateProfile(Long personalDetailId, Long attachmentId) {
        PersonalDetail personalDetail = authService.getPersonalDetailById(personalDetailId);
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);

        personalDetail.updateProfile(attachment.getUploadFilePath());
        attachment.updatePersonalDetailId(personalDetailId);

        attachmentService.saveAttachment(attachment);
        authService.savePersonalDetail(personalDetail);

        if (personalDetail.getUserRole() == UserRole.MEMBER) {
            Member member = memberService.getMember(personalDetail.getMemberId());
            member.updateProfile(attachment.getUploadFilePath());
            memberService.saveMember(member);
        }
    }
}
