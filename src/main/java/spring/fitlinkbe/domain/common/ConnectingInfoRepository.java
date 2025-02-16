package spring.fitlinkbe.domain.common;

import spring.fitlinkbe.domain.common.model.ConnectingInfo;

import java.util.Optional;

public interface ConnectingInfoRepository {
    ConnectingInfo getConnectingInfo(Long memberId, Long trainerId);

    ConnectingInfo save(ConnectingInfo connectingInfo);

    Optional<ConnectingInfo> getExistConnectingInfo(Long memberId);
}
