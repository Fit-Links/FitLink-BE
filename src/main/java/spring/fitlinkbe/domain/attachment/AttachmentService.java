package spring.fitlinkbe.domain.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring.fitlinkbe.domain.attachment.model.Attachment;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public Attachment saveAttachment(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }
}
