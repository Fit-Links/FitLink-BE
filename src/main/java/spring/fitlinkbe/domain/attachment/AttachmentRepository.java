package spring.fitlinkbe.domain.attachment;

import spring.fitlinkbe.domain.attachment.model.Attachment;

public interface AttachmentRepository {
    Attachment save(Attachment attachment);
}
