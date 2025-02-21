package spring.fitlinkbe.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.application.member.criteria.MemberInfoResult;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional
public class MemberFacade {
    private final MemberService memberService;
    private final TrainerService trainerService;
    private final NotificationService notificationService;

    public void connectTrainer(Long memberId, String trainerCode) {
        memberService.checkMemberAlreadyConnected(memberId);

        Trainer trainer = trainerService.getTrainerByCode(trainerCode);
        Member member = memberService.getMember(memberId);

        ConnectingInfo connectingInfo = memberService.requestConnectTrainer(trainer, member);
        PersonalDetail trainerDetail = trainerService.getTrainerDetail(trainer.getTrainerId());
        notificationService.sendConnectRequestNotification(trainerDetail, member.getName(), connectingInfo.getConnectingInfoId());
    }

    public void disconnectTrainer(Long memberId) {
        ConnectingInfo connectingInfo = memberService.getConnectedInfo(memberId);
        if (connectingInfo.isPending()) {
            throw new CustomException(ErrorCode.DISCONNECT_AVAILABLE_AFTER_ACCEPTED);
        }

        Member member = memberService.getMember(memberId);
        PersonalDetail trainerDetail = trainerService.getTrainerDetail(connectingInfo.getTrainer().getTrainerId());
        notificationService.sendDisconnectNotification(member.getName(), trainerDetail);

        connectingInfo.disconnect();
        memberService.saveConnectingInfo(connectingInfo);
    }

    @Transactional(readOnly = true)
    public MemberInfoResult.Response getMyInfo(Long memberId) {
        Optional<ConnectingInfo> connectingInfo = memberService.findConnectedInfo(memberId);
        Optional<SessionInfo> sessionInfo = memberService.findSessionInfo(memberId);

        Member me = memberService.getMember(memberId);
        Trainer trainer = connectingInfo.map(ConnectingInfo::getTrainer).orElse(null);

        return MemberInfoResult.Response.of(me, trainer, sessionInfo.orElse(null));
    }
}
