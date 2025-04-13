package spring.fitlinkbe.domain.attachment;

import spring.fitlinkbe.domain.attachment.model.Attachment;

import java.util.Optional;

public interface AttachmentRepository {
    Attachment save(Attachment attachment);

    Optional<Attachment> findById(Long attachmentId);
}
