package spring.fitlinkbe.infra.attachment;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import spring.fitlinkbe.domain.attachment.AttachmentRepository;
import spring.fitlinkbe.domain.attachment.model.Attachment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AttachmentRepositoryImpl implements AttachmentRepository {

    private final AttachmentJpaRepository attachmentJpaRepository;
    private final EntityManager em;
    private final AwsRegionProvider regionProvider;

    @Override
    public Attachment save(Attachment attachment) {
        AttachmentEntity attachmentEntity = AttachmentEntity.of(attachment, em);

        return attachmentJpaRepository.save(attachmentEntity).toDomain();
    }

    @Override
    public Optional<Attachment> findById(Long attachmentId) {
        return attachmentJpaRepository.findById(attachmentId)
                .map(AttachmentEntity::toDomain);
    }

    @Override
    public List<Attachment> findPendingAttachment(LocalDateTime threshold) {
        return attachmentJpaRepository.findPendingAttachment(threshold)
                .stream()
                .map(AttachmentEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteAll(List<Attachment> pendingAttachments) {
        attachmentJpaRepository.deleteAllById(
                pendingAttachments.stream().map(Attachment::getAttachmentId).toList()
        );
    }
}
