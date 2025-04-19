package spring.fitlinkbe.infra.attachment;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.attachment.AttachmentRepository;
import spring.fitlinkbe.domain.attachment.model.Attachment;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AttachmentRepositoryImpl implements AttachmentRepository {

    private final AttachmentJpaRepository attachmentJpaRepository;
    private final EntityManager em;

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
}
