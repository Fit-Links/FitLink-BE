package spring.fitlinkbe.domain.common;

import spring.fitlinkbe.domain.common.model.SessionInfo;

import java.util.Optional;

public interface SessionInfoRepository {

    Optional<SessionInfo> saveSessionInfo(SessionInfo sessionInfo);

    Optional<SessionInfo> getSessionInfo(long sessionInfoId);

    Optional<SessionInfo> findSessionInfo(Long memberId);
}
