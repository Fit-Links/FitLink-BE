package spring.fitlinkbe.infra.common;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail.OauthProvider;
import spring.fitlinkbe.infra.common.model.PersonalDetailEntity;

import java.util.Optional;

public interface PersonalDetailJpaRepository extends JpaRepository<PersonalDetailEntity, Long> {
    Optional<PersonalDetailEntity> findByOauthProviderAndProviderId(OauthProvider oauthProvider, String providerId);
}
