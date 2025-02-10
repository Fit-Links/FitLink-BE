package spring.fitlinkbe.integration.common;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.SessionInfoRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.reservation.ReservationRepository;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;

import java.util.UUID;

@Component
@Transactional
public class TestDataHandler {

    private final ReservationRepository reservationRepository;

    private final PersonalDetailRepository personalDetailRepository;

    private final MemberRepository memberRepository;

    private final TrainerRepository trainerRepository;

    private final SessionInfoRepository sessionInfoRepository;

    public TestDataHandler(ReservationRepository reservationRepository,
                           PersonalDetailRepository personalDetailRepository,
                           MemberRepository memberRepository, TrainerRepository trainerRepository,
                           SessionInfoRepository sessionInfoRepository) {
        this.reservationRepository = reservationRepository;
        this.personalDetailRepository = personalDetailRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.sessionInfoRepository = sessionInfoRepository;
    }

    public PersonalDetail createPersonalDetail(
            PersonalDetail.Status status
    ) {
        PersonalDetail personalDetail = PersonalDetail.builder()
                .name("홍길동")
                .email("test@testcode.co.kr")
                .status(status)
                .oauthProvider(PersonalDetail.OauthProvider.GOOGLE)
                .providerId(UUID.randomUUID().toString())
                .build();

        return personalDetailRepository.savePersonalDetail(personalDetail).orElseThrow();
    }

    public void settingUserInfo() {

        Trainer trainer = Trainer.builder().trainerCode("1234").build();

        Trainer savedTrainer = trainerRepository.saveTrainer(trainer).orElseThrow();

        Member member1 = Member.builder()
                .name("김민수")
                .trainer(savedTrainer)
                .build();

        Member savedMember1 = memberRepository.saveMember(member1).orElseThrow();

        personalDetailRepository.savePersonalDetail(PersonalDetail.builder()
                .providerId("1")
                .oauthProvider(PersonalDetail.OauthProvider.GOOGLE)
                .trainer(savedTrainer)
                .member(null)
                .build());

        personalDetailRepository.savePersonalDetail(PersonalDetail.builder()
                .member(savedMember1)
                .trainer(null)
                .providerId("1")
                .oauthProvider(PersonalDetail.OauthProvider.GOOGLE)
                .build());

    }

    public void settingSessionInfo() {

        Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();
        Member member = memberRepository.getMember(1L).orElseThrow();
        SessionInfo sessionInfo = SessionInfo.builder()
                .trainer(trainer)
                .member(member)
                .build();

        sessionInfoRepository.saveSessionInfo(sessionInfo);
    }
}
