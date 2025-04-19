package spring.fitlinkbe.infra.common.sessioninfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.SessionInfoRepository;
import spring.fitlinkbe.domain.common.model.SessionInfo;

import java.util.List;
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

    @Override
    public Optional<SessionInfo> getSessionInfo(long sessionInfoId) {

        Optional<SessionInfoEntity> findEntity = sessionInfoJpaRepository.findByIdJoinFetch(sessionInfoId);

        if (findEntity.isPresent()) {
            return findEntity.map(SessionInfoEntity::toDomain);
        }

        return Optional.empty();
    }

    @Override
    public Optional<SessionInfo> getSessionInfo(Long trainerId, Long memberId) {
        Optional<SessionInfoEntity> findEntity = sessionInfoJpaRepository
                .findWithPessimisticLock(memberId, trainerId);

        if (findEntity.isPresent()) {
            return findEntity.map(SessionInfoEntity::toDomain);
        }
        return Optional.empty();
    }

    @Override
    public Optional<SessionInfo> findSessionInfo(Long memberId) {

        Optional<SessionInfoEntity> findEntity = sessionInfoJpaRepository.findByMemberIdJoinFetch(memberId);

        if (findEntity.isPresent()) {
            return findEntity.map(SessionInfoEntity::toDomain);
        }

        return Optional.empty();
    }

    @Override
    public List<SessionInfo> findAllSessionInfo(List<Long> memberIds, Long trainerId) {
        List<SessionInfoEntity> findEntities = sessionInfoJpaRepository
                .findByMember_memberIdInAndTrainer_TrainerId(memberIds, trainerId);

        return findEntities.stream()
                .map(SessionInfoEntity::toDomain)
                .toList();
    }
}
