package spring.fitlinkbe.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;

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
}
