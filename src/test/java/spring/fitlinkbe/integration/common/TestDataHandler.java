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
import spring.fitlinkbe.domain.member.WorkoutSchedule;
import spring.fitlinkbe.domain.member.WorkoutScheduleRepository;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationRepository;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.domain.trainer.DayOff;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;
import spring.fitlinkbe.support.security.AuthTokenProvider;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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

    private final WorkoutScheduleRepository workoutScheduleRepository;

    private final ReservationRepository reservationRepository;

    public TestDataHandler(
            PersonalDetailRepository personalDetailRepository,
            MemberRepository memberRepository, TrainerRepository trainerRepository,
            SessionInfoRepository sessionInfoRepository, AuthTokenProvider authTokenProvider,
            ConnectingInfoRepository connectingInfoRepository, WorkoutScheduleRepository workoutScheduleRepository,
            ReservationRepository reservationRepository) {
        this.personalDetailRepository = personalDetailRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.sessionInfoRepository = sessionInfoRepository;
        this.authTokenProvider = authTokenProvider;
        this.connectingInfoRepository = connectingInfoRepository;
        this.workoutScheduleRepository = workoutScheduleRepository;
        this.reservationRepository = reservationRepository;
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

    public Member createMember(PersonalDetail.Status status) {
        Member member = Member.builder()
                .name("김민수")
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNumber(new PhoneNumber("01012345678"))
                .isConnected(false)
                .build();

        Member saved = memberRepository.saveMember(member).orElseThrow();
        createPersonalDetail(status, saved.getMemberId());
        return saved;
    }

    public Member createMember(String name) {
        Member member = Member.builder()
                .name(name)
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
                .trainerId(trainer.getTrainerId())
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
                .memberId(member.getMemberId())
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

    public PersonalDetail createPersonalDetail(
            PersonalDetail.Status status,
            Long memberId
    ) {
        PersonalDetail personalDetail = PersonalDetail.builder()
                .name("홍길동")
                .email("test@testcode.co.kr")
                .memberId(memberId)
                .status(status)
                .oauthProvider(PersonalDetail.OauthProvider.GOOGLE)
                .providerId(UUID.randomUUID().toString())
                .build();

        return personalDetailRepository.savePersonalDetail(personalDetail).orElseThrow();
    }

    public void settingUserInfo() {

        Trainer trainer = Trainer.builder()
                .trainerCode("1234")
                .name("트레이너황")
                .build();

        Trainer savedTrainer = trainerRepository.saveTrainer(trainer).orElseThrow();

        Member member1 = Member.builder()
                .name("김민수")
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNumber(new PhoneNumber("01012345678"))
                .trainer(savedTrainer)
                .build();

        Member savedMember1 = memberRepository.saveMember(member1).orElseThrow();

        personalDetailRepository.savePersonalDetail(PersonalDetail.builder()
                .providerId("1")
                .oauthProvider(PersonalDetail.OauthProvider.GOOGLE)
                .trainerId(savedTrainer.getTrainerId())
                .name(savedTrainer.getName())
                .memberId(null)
                .build());

        personalDetailRepository.savePersonalDetail(PersonalDetail.builder()
                .memberId(savedMember1.getMemberId())
                .name(savedMember1.getName())
                .trainerId(null)
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
                .totalCount(10)
                .remainingCount(6)
                .build();

        sessionInfoRepository.saveSessionInfo(sessionInfo);
    }

    public String createTokenFromMember(Member member) {
        PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(member.getMemberId()).orElseThrow();
        return authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                personalDetail.getUserRole());
    }

    public String createTokenFromTrainer(Trainer trainer) {
        PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(trainer.getTrainerId()).orElseThrow();
        return authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                personalDetail.getUserRole());
    }

    public PersonalDetail getTrainerPersonalDetail(Long trainerId) {
        return personalDetailRepository.getTrainerDetail(trainerId).orElseThrow();
    }

    public PersonalDetail getMemberPersonalDetail(Long memberId) {
        return personalDetailRepository.getMemberDetail(memberId).orElseThrow();
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

    public SessionInfo createSessionInfo(Member member, Trainer trainer) {
        SessionInfo sessionInfo = SessionInfo.builder()
                .member(member)
                .trainer(trainer)
                .remainingCount(10)
                .totalCount(10)
                .build();

        return sessionInfoRepository.saveSessionInfo(sessionInfo).orElseThrow();
    }

    public List<WorkoutSchedule> createWorkoutSchedules(Member member) {
        WorkoutSchedule workoutSchedule1 = WorkoutSchedule.builder()
                .member(member)
                .dayOfWeek(DayOfWeek.MONDAY)
                .preferenceTimes(List.of(LocalTime.of(10, 0), LocalTime.of(12, 0)))
                .build();

        WorkoutSchedule workoutSchedule2 = WorkoutSchedule.builder()
                .member(member)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .preferenceTimes(List.of(LocalTime.of(10, 0), LocalTime.of(14, 0)))
                .build();

        WorkoutSchedule workoutSchedule3 = WorkoutSchedule.builder()
                .member(member)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .preferenceTimes(List.of(LocalTime.of(12, 0), LocalTime.of(14, 0)))
                .build();

        WorkoutSchedule workoutSchedule4 = WorkoutSchedule.builder()
                .member(member)
                .dayOfWeek(DayOfWeek.THURSDAY)
                .preferenceTimes(List.of(LocalTime.of(10, 0), LocalTime.of(13, 0)))
                .build();

        WorkoutSchedule workoutSchedule5 = WorkoutSchedule.builder()
                .member(member)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .preferenceTimes(List.of(LocalTime.of(11, 0), LocalTime.of(12, 0)))
                .build();

        return workoutScheduleRepository.saveAll(
                List.of(workoutSchedule1, workoutSchedule2, workoutSchedule3, workoutSchedule4, workoutSchedule5)
        );
    }

    public Session createSession(Member member, Trainer trainer, Session.Status status) {
        Reservation reservation = Reservation.builder()
                .member(member)
                .trainer(trainer)
                .status(Reservation.Status.RESERVATION_APPROVED)
                .reservationDates(List.of(LocalDateTime.now()))
                .build();
        Reservation saved = reservationRepository.saveReservation(reservation).orElseThrow();

        Session session = Session.builder()
                .reservation(saved)
                .status(status)
                .isCompleted(true)
                .build();
        return reservationRepository.saveSession(session).orElseThrow();
    }

    public void createAvailableTime(Trainer trainer, DayOfWeek dayOfWeek, LocalDate applyAt) {
        AvailableTime availableTime = AvailableTime.builder()
                .trainer(trainer)
                .dayOfWeek(dayOfWeek)
                .applyAt(applyAt)
                .isHoliday(false)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        trainerRepository.saveAvailableTime(availableTime);
    }

    public DayOff createDayOff(Trainer trainer, LocalDate dayOffDate) {
        DayOff dayOff = DayOff.builder()
                .trainer(trainer)
                .dayOffDate(dayOffDate)
                .build();

        return trainerRepository.saveDayOff(dayOff).get();
    }

    public void createReservation(Member member, Trainer trainer, LocalDate reservationDate) {
        Reservation reservation = Reservation.builder()
                .trainer(trainer)
                .member(member)
                .status(Reservation.Status.RESERVATION_APPROVED)
                .reservationDates(
                        List.of(
                                LocalDateTime.of(reservationDate, LocalTime.of(10, 0)),
                                LocalDateTime.of(reservationDate.plusDays(1), LocalTime.of(10, 13))
                        )
                )
                .build();

        reservationRepository.saveReservation(reservation);
    }

    public void createConfirmReservation(Member member, Trainer trainer, LocalDateTime confirmDate) {
        Reservation reservation = Reservation.builder()
                .trainer(trainer)
                .member(member)
                .status(Reservation.Status.RESERVATION_APPROVED)
                .confirmDate(confirmDate)
                .build();

        reservationRepository.saveReservation(reservation);
    }
}
