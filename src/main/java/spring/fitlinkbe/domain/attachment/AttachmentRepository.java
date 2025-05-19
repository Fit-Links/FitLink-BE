package spring.fitlinkbe.domain.attachment;

import spring.fitlinkbe.domain.attachment.model.Attachment;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AttachmentRepository {
    Attachment save(Attachment attachment);

    Optional<Attachment> findById(Long attachmentId);

    public List<Attachment> findPendingAttachment(LocalDateTime threshold);

    void deleteAll(List<Attachment> pendingAttachments);

}
