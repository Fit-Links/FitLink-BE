package spring.fitlinkbe.domain.common;

import spring.fitlinkbe.domain.common.model.ConnectingInfo;

import java.util.Optional;

public interface ConnectingInfoRepository {
    ConnectingInfo getConnectingInfo(Long memberId, Long trainerId);

    ConnectingInfo save(ConnectingInfo connectingInfo);

    /**
     * 해당 회원의 연결된 또는, 연결 요청중인 트레이너 연결 정보를 가져온다.
     *
     * @param memberId
     * @return
     */
    Optional<ConnectingInfo> getConnectedInfo(Long memberId);

}
