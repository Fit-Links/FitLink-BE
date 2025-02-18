package spring.fitlinkbe.integration.common;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.ConnectingInfoRepository;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.SessionInfoRepository;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PhoneNumber;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;
import spring.fitlinkbe.support.security.AuthTokenProvider;

import java.time.LocalDate;
import java.util.UUID;

@Component
@Transactional
public class TestDataHandler {


    private final PersonalDetailRepository personalDetailRepository;

    private final MemberRepository memberRepository;

    private final TrainerRepository trainerRepository;

    private final SessionInfoRepository sessionInfoRepository;

    private final AuthTokenProvider authTokenProvider;

    private final ConnectingInfoRepository connectingInfoRepository;

    public TestDataHandler(
            PersonalDetailRepository personalDetailRepository,
            MemberRepository memberRepository, TrainerRepository trainerRepository,
            SessionInfoRepository sessionInfoRepository, AuthTokenProvider authTokenProvider, ConnectingInfoRepository connectingInfoRepository) {
        this.personalDetailRepository = personalDetailRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.sessionInfoRepository = sessionInfoRepository;
        this.authTokenProvider = authTokenProvider;
        this.connectingInfoRepository = connectingInfoRepository;
    }

    public Member createMember() {
        Member member = Member.builder()
                .name("김민수")
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNumber(new PhoneNumber("01012345678"))
                .isConnected(false)
                .build();

        Member saved = memberRepository.saveMember(member).orElseThrow();
        createPersonalDetail(saved);
        return saved;
    }

    public Trainer createTrainer(String trainerCode) {
        Trainer trainer = Trainer.builder()
                .trainerCode(trainerCode)
                .build();

        Trainer saved = trainerRepository.saveTrainer(trainer).orElseThrow();
        createPersonalDetail(saved);
        return saved;
    }

    public void createPersonalDetail(Trainer trainer) {
        PersonalDetail personalDetail = PersonalDetail.builder()
                .name("홍길동")
                .email("test@testcode.co.kr")
                .status(PersonalDetail.Status.NORMAL)
                .trainer(trainer)
                .oauthProvider(PersonalDetail.OauthProvider.GOOGLE)
                .providerId(UUID.randomUUID().toString())
                .build();
        personalDetailRepository.savePersonalDetail(personalDetail).orElseThrow();
    }

    public void createPersonalDetail(Member member) {
        PersonalDetail personalDetail = PersonalDetail.builder()
                .name("홍길동")
                .email("test@testcode.co.kr")
                .status(PersonalDetail.Status.NORMAL)
                .member(member)
                .oauthProvider(PersonalDetail.OauthProvider.GOOGLE)
                .providerId(UUID.randomUUID().toString())
                .build();

        personalDetailRepository.savePersonalDetail(personalDetail).orElseThrow();
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

    public String createTokenFromMember(Member member) {
        PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(member.getMemberId()).orElseThrow();
        return authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId());
    }

    public PersonalDetail getPersonalDetail(Long trainerId) {
        return personalDetailRepository.getTrainerDetail(trainerId).orElseThrow();
    }

    public void connectMemberAndTrainer(Member member, Trainer trainer) {
        ConnectingInfo connectingInfo = ConnectingInfo.builder()
                .member(member)
                .trainer(trainer)
                .status(ConnectingInfo.ConnectingStatus.CONNECTED)
                .build();
        connectingInfoRepository.save(connectingInfo);
    }

    public void requestConnectTrainer(Member member, Trainer trainer) {
        ConnectingInfo connectingInfo = ConnectingInfo.builder()
                .member(member)
                .trainer(trainer)
                .status(ConnectingInfo.ConnectingStatus.REQUESTED)
                .build();
        connectingInfoRepository.save(connectingInfo);
    }
}
