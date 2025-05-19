package spring.fitlinkbe.domain.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.attachment.model.Attachment;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.support.utils.ClockUtils;

import java.time.LocalDateTime;
import java.util.List;

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

    public void deletePendingAttachments() {
        List<Attachment> pendingAttachments = findNotUploadedAttachments();

        attachmentRepository.deleteAll(pendingAttachments);
    }

    private List<Attachment> findNotUploadedAttachments() {
        LocalDateTime tenMinutesAgo = ClockUtils.localDateTimeNow().minusMinutes(10);
        return attachmentRepository.findPendingAttachment(tenMinutesAgo);
    }

    /**
     * Finds the attachment by ID and links the personal detail ID to it.
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
