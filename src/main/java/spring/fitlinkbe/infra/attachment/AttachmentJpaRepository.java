package spring.fitlinkbe.infra.attachment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentJpaRepository extends JpaRepository<AttachmentEntity, Long> {
}
