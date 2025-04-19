package spring.fitlinkbe.domain.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.attachment.model.Attachment;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public Attachment saveAttachment(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    public Attachment getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ATTACHMENT_NOT_FOUND));
    }

    /**
     * Finds the attachment by ID and updates the personal detail ID.
     */
    public Attachment findAttachment(Long attachmentId, Long personalDetailId) {
        if (attachmentId == null) {
            return null;
        }
        Attachment attachment = getAttachmentById(attachmentId);
        attachment.updatePersonalDetailId(personalDetailId);

        saveAttachment(attachment);
        return attachment;
    }
}
