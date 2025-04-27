package spring.fitlinkbe.infra.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.fitlinkbe.domain.outbox.Outbox;

import java.util.List;
import java.util.Optional;

public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, Long> {
    List<OutboxEntity> findByEventStatusIs(Outbox.EventStatus status);

    Optional<OutboxEntity> findByMessageId(String messageId);
}
