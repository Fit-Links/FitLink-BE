package spring.fitlinkbe.infra.common.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenJpaRepository extends JpaRepository<TokenEntity, Long> {
    Optional<TokenEntity> findByPersonalDetail_PersonalDetailId(Long personalDetailId);
}
