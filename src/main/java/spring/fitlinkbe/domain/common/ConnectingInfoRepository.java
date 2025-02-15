package spring.fitlinkbe.domain.common;

import spring.fitlinkbe.domain.common.model.ConnectingInfo;

public interface ConnectingInfoRepository {
    ConnectingInfo getConnectingInfo(Long memberId, Long trainerId);

    void save(ConnectingInfo connectingInfo);
}
