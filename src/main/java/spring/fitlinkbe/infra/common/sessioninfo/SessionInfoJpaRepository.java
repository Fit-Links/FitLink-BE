package spring.fitlinkbe.infra.common.sessioninfo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionInfoJpaRepository extends JpaRepository<SessionInfoEntity, Long> {
}
