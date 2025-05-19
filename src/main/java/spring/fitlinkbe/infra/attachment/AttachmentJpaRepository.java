package spring.fitlinkbe.infra.attachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AttachmentJpaRepository extends JpaRepository<AttachmentEntity, Long> {

    @Query("SELECT a " +
            "FROM AttachmentEntity a " +
            "WHERE a.createdAt <= :threshold " +
            "  AND a.isUploaded = false")
    List<AttachmentEntity> findPendingAttachment(@Param("threshold") LocalDateTime threshold);
}
