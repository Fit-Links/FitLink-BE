package spring.fitlinkbe.infra.common.connectingInfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.ConnectingInfoRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class ConnectingInfoRepositoryImpl implements ConnectingInfoRepository {

    private final ConnectingInfoJpaRepository connectingInfoJpaRepository;

    @Override
    public ConnectingInfo getConnectingInfo(Long memberId, Long trainerId) {
        return connectingInfoJpaRepository.findByMember_MemberIdAndTrainer_TrainerId(memberId, trainerId)
                .map(ConnectingInfoEntity::toDomain)
                .orElseThrow(() -> new CustomException(ErrorCode.CONNECTING_INFO_NOT_FOUND));
    }

    @Override
    public ConnectingInfo save(ConnectingInfo connectingInfo) {
        return connectingInfoJpaRepository.save(ConnectingInfoEntity.from(connectingInfo)).toDomain();
    }

    @Override
    public Optional<ConnectingInfo> getConnectedInfo(Long memberId) {
        return connectingInfoJpaRepository.findExistMemberConnectingInfo(memberId)
                .map(ConnectingInfoEntity::toDomain);
    }

    @Override
    public Optional<ConnectingInfo> findConnectingInfo(Long trainerId, Long memberId) {
        return connectingInfoJpaRepository.findByTrainer_TrainerIdAndMember_MemberId(trainerId, memberId)
                .map(ConnectingInfoEntity::toDomain);
    }
}
