package spring.fitlinkbe.integration.common;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.SessionInfoRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationRepository;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;

import java.time.LocalDateTime;

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


    public void settingReservationInfo() {

        Trainer trainer = Trainer.builder().trainerCode("1234").build();

        Trainer savedTrainer = trainerRepository.saveTrainer(trainer).orElseThrow();

        Member member1 = Member.builder()
                .name("김민수")
                .trainer(savedTrainer)
                .build();

        Member member2 = Member.builder()
                .name("김영희")
                .trainer(savedTrainer)
                .build();

        Member savedMember1 = memberRepository.saveMember(member1).orElseThrow();
        Member savedMember2 = memberRepository.saveMember(member2).orElseThrow();

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

        personalDetailRepository.savePersonalDetail(PersonalDetail.builder()
                .member(savedMember2)
                .trainer(null)
                .providerId("1")
                .oauthProvider(PersonalDetail.OauthProvider.GOOGLE)
                .build());

        SessionInfo sessionInfo1 = SessionInfo.builder()
                .trainer(savedTrainer)
                .member(savedMember1)
                .remainCount(10)
                .totalCount(50)
                .build();

        SessionInfo sessionInfo2 = SessionInfo.builder()
                .trainer(savedTrainer)
                .member(savedMember2)
                .remainCount(10)
                .totalCount(50)
                .build();

        SessionInfo savedSessionInfo1 = sessionInfoRepository.saveSessionInfo(sessionInfo1).orElseThrow();
        SessionInfo savedSessionInfo2 = sessionInfoRepository.saveSessionInfo(sessionInfo2).orElseThrow();

        LocalDateTime date1 = LocalDateTime.now().plusWeeks(2).minusDays(1).minusSeconds(1);
        LocalDateTime date2 = LocalDateTime.now().plusMonths(1).minusSeconds(1);

        Reservation reservation1 = Reservation.builder()
                .reservationDate(date1)
                .trainerId(savedTrainer.getTrainerId())
//                .trainer(savedTrainer)
                .memberId(savedMember1.getMemberId())
//                .member(savedMember1)
                .name(savedMember1.getName())
                .dayOfWeek(date1.getDayOfWeek())
//                .sessionInfo(savedSessionInfo1)
                .sessionInfoId(savedSessionInfo1.getSessionInfoId())
                .priority(0)
                .build();

        Reservation reservation2 = Reservation.builder()
                .reservationDate(date2)
                .trainerId(savedTrainer.getTrainerId())
//                .trainer(savedTrainer)
                .memberId(savedMember1.getMemberId())
//                .member(savedMember1)
                .name(savedMember1.getName())
                .dayOfWeek(date2.getDayOfWeek())
//                .sessionInfo(savedSessionInfo1)
                .sessionInfoId(savedSessionInfo1.getSessionInfoId())
                .priority(0)
                .build();

        Reservation reservation3 = Reservation.builder()
                .reservationDate(date1)
                .trainerId(savedTrainer.getTrainerId())
//                .trainer(savedTrainer)
                .memberId(savedMember2.getMemberId())
//                .member(savedMember1)
                .name(savedMember2.getName())
                .dayOfWeek(date1.getDayOfWeek())
//                .sessionInfo(savedSessionInfo1)
                .sessionInfoId(savedSessionInfo2.getSessionInfoId())
                .priority(0)
                .build();

        Reservation reservation4 = Reservation.builder()
                .reservationDate(date2)
                .trainerId(savedTrainer.getTrainerId())
//                .trainer(savedTrainer)
                .memberId(savedMember2.getMemberId())
//                .member(savedMember1)
                .name(savedMember2.getName())
                .dayOfWeek(date2.getDayOfWeek())
//                .sessionInfo(savedSessionInfo1)
                .sessionInfoId(savedSessionInfo2.getSessionInfoId())
                .priority(0)
                .build();

        reservationRepository.saveReservation(reservation1);
        reservationRepository.saveReservation(reservation2);
        reservationRepository.saveReservation(reservation3);
        reservationRepository.saveReservation(reservation4);
    }

}
