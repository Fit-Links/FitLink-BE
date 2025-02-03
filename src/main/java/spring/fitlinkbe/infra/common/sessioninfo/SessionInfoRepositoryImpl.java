package spring.fitlinkbe.infra.common.sessioninfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.SessionInfoRepository;
import spring.fitlinkbe.domain.common.model.SessionInfo;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SessionInfoRepositoryImpl implements SessionInfoRepository {

    private final SessionInfoJpaRepository sessionInfoJpaRepository;


    @Override
    public Optional<SessionInfo> saveSessionInfo(SessionInfo sessionInfo) {
        SessionInfoEntity savedEntity = sessionInfoJpaRepository.save(SessionInfoEntity.from(sessionInfo));

        return Optional.of(savedEntity.toDomain());
    }
}
