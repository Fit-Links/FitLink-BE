package spring.fitlinkbe.infra.common;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.fitlinkbe.infra.common.model.TokenEntity;

import java.util.Optional;

public interface TokenJpaRepository extends JpaRepository<TokenEntity, Long> {
    Optional<TokenEntity> findByPersonalDetail_PersonalDetailId(Long personalDetailId);
}
