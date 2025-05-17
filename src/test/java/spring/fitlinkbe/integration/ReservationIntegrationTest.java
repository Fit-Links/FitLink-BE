package spring.fitlinkbe.integration;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.SessionInfoRepository;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationRepository;
import spring.fitlinkbe.domain.outbox.Outbox;
import spring.fitlinkbe.domain.outbox.OutboxRepository;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationRepository;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.trainer.DayOff;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationRequestDto;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationResponseDto;
import spring.fitlinkbe.interfaces.scheduler.ReservationScheduler;
import spring.fitlinkbe.support.security.AuthTokenProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static spring.fitlinkbe.domain.notification.Notification.NotificationType.*;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_CANCEL_REQUEST;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_CANCEL_REQUEST_REFUSED;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_CHANGE_REQUEST;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.*;
import static spring.fitlinkbe.domain.reservation.Session.Status.SESSION_COMPLETED;
import static spring.fitlinkbe.domain.reservation.Session.Status.*;

public class ReservationIntegrationTest extends BaseIntegrationTest {

    private static final String PATH = "/v1/reservations";

    @Autowired
    AuthTokenProvider tokenProvider;

    @Autowired
    PersonalDetailRepository personalDetailRepository;

    @Autowired
    TrainerRepository trainerRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    SessionInfoRepository sessionInfoRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OutboxRepository outboxRepository;

    @Autowired
    TestDataHandler testDataHandler;

    @Autowired
    ReservationScheduler reservationScheduler;


    @BeforeEach
    void setUp() {
        testDataHandler.settingUserInfo();
        testDataHandler.settingSessionInfo();
        testDataHandler.createTokenInfo();
    }

    @Nested
    @DisplayName("예약 목록 조회 Integration TEST")
    class GetReservationsIntegrationTest {

        @Test
        @DisplayName("트레이너는 2주 안에 예약 목록이 있을 경우 목록을 조회한다.")
        void getReservationsWithTrainerIn2Weeks() {
            // given
            Map<String, String> params = new HashMap<>();
            params.put("date", LocalDate.now().toString());

            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            LocalDateTime reqeustDate = LocalDateTime.now().plusWeeks(2).minusDays(1).minusSeconds(1);

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reqeustDate))
                    .trainer(trainer)
                    .member(member)
                    .sessionInfo(sessionInfo)
                    .name(member.getName())
                    .dayOfWeek(reqeustDate.getDayOfWeek())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Summary.class);
                softly.assertThat(content.size()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("트레이너의 휴무일도 예약 목록에 같이 나온다.")
        void getReservationsWithTrainerDayOff() {
            // given
            Map<String, String> params = new HashMap<>();
            params.put("date", LocalDate.now().toString());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            LocalDate dayOffDate = LocalDate.now().plusDays(1);

            DayOff dayOff = DayOff.builder()
                    .trainer(trainer)
                    .dayOffDate(LocalDate.now().plusDays(1))
                    .build();

            trainerRepository.saveDayOff(dayOff);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(dayOffDate.atStartOfDay()))
                    .trainer(trainer)
                    .isDayOff(true)
                    .status(DISABLED_TIME_RESERVATION)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation);

            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Summary.class);
                softly.assertThat(content.size()).isEqualTo(1);
                softly.assertThat(content.get(0).isDayOff()).isTrue();

            });
        }

        @Test
        @DisplayName("트레이너는 15일 뒤에 예약 목록이 있을 경우 조회 하지 못한다.")
        void getReservationsWithTrainerAfter15Days() {
            // given
            Map<String, String> params = new HashMap<>();
            params.put("date", LocalDate.now().toString());

            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reqeustDate = LocalDateTime.now().plusWeeks(2);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reqeustDate))
                    .trainer(trainer)
                    .member(member)
                    .sessionInfo(sessionInfo)
                    .name(member.getName())
                    .dayOfWeek(reqeustDate.getDayOfWeek())
                    .build();

            reservationRepository.saveReservation(reservation);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Summary.class);
                softly.assertThat(content.size()).isSameAs(0);
            });
        }

        @Test
        @DisplayName("멤버는 1달 안에 예약 목록이 있을 경우 목록을 조회한다.")
        void getReservationsWithMemberIn1Month() {
            // given
            Map<String, String> params = new HashMap<>();
            params.put("date", LocalDate.now().toString());

            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reqeustDate = LocalDateTime.now().plusMonths(1).minusDays(1).minusSeconds(1);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reqeustDate))
                    .trainer(trainer)
                    .member(member)
                    .status(RESERVATION_WAITING)
                    .sessionInfo(sessionInfo)
                    .name(member.getName())
                    .dayOfWeek(reqeustDate.getDayOfWeek())
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Summary.class);
                softly.assertThat(content.size()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("멤버는 1달하고 하루 뒤에 예약 목록을 조회할 경우 조회 하지 못한다.")
        void getReservationsWithMemberIn1MonthWithEmptyDate() {
            // given
            Map<String, String> params = new HashMap<>();
            params.put("date", LocalDate.now().toString());

            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reqeustDate = LocalDateTime.now().plusMonths(1).minusSeconds(1);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reqeustDate))
                    .trainer(trainer)
                    .member(member)
                    .sessionInfo(sessionInfo)
                    .name(member.getName())
                    .dayOfWeek(reqeustDate.getDayOfWeek())
                    .build();

            reservationRepository.saveReservation(reservation);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Summary.class);
                softly.assertThat(content.size()).isEqualTo(0);
            });
        }

    }

    @Nested
    @DisplayName("예약 상세 조회 Integration TEST")
    class GetReservationDetailIntegrationTest {

        @Test
        @DisplayName("트레이너는 예약 상세 목록을 조회한다.")
        void getReservationDetailWithTrainer() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reqeustDate = LocalDateTime.now().plusMonths(1).minusSeconds(1);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reqeustDate))
                    .trainer(trainer)
                    .member(member)
                    .sessionInfo(sessionInfo)
                    .name(member.getName())
                    .dayOfWeek(reqeustDate.getDayOfWeek())
                    .status(RESERVATION_WAITING)
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/"
                    + savedReservation.getReservationId(), accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Detail.class)
                                .reservationId())
                        .isEqualTo(1L);
            });
        }

        @Test
        @DisplayName("멤버가 예약 상세 목록을 조회한다.")
        void getReservationDetailWithMember() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reqeustDate = LocalDateTime.now().plusMonths(1).minusSeconds(1);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reqeustDate))
                    .trainer(trainer)
                    .member(member)
                    .sessionInfo(sessionInfo)
                    .name(member.getName())
                    .dayOfWeek(reqeustDate.getDayOfWeek())
                    .status(RESERVATION_WAITING)
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/"
                    + savedReservation.getReservationId(), accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Detail.class)
                                .reservationId())
                        .isEqualTo(1L);
            });
        }

        @Test
        @DisplayName("예약이 승낙이 됐다면, 세션 정보도 같이 상세 목록에 조회한다.")
        void getReservationDetailWithApproved() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reqeustDate = LocalDateTime.now().plusMonths(1).minusSeconds(1);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reqeustDate))
                    .trainer(trainer)
                    .member(member)
                    .sessionInfo(sessionInfo)
                    .name(member.getName())
                    .dayOfWeek(reqeustDate.getDayOfWeek())
                    .status(RESERVATION_APPROVED)
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            Session session = Session.builder()
                    .reservation(savedReservation)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/"
                    + savedReservation.getReservationId(), accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Detail.class)
                                .sessionId())
                        .isEqualTo(1L);
            });
        }
    }

    @Nested
    @DisplayName("예약 상세 대기 목록 조회 Integration TEST")
    class WaitingMembersIntegrationTest {
        @Test
        @DisplayName("트레이너 예약 상세 대기 목록 조회 성공")
        void getReservationWaitingMembersWithTrainer() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member1 = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo1 = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reserveDate = LocalDateTime.now().plusDays(1);

            Reservation reservation1 = Reservation.builder()
                    .reservationDates(List.of(reserveDate))
                    .trainer(trainer)
                    .member(member1)
                    .sessionInfo(sessionInfo1)
                    .name(member1.getName())
                    .dayOfWeek(reserveDate.getDayOfWeek())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation1 = reservationRepository.saveReservation(reservation1).orElseThrow();

            Member member2 = testDataHandler.createMember();
            SessionInfo sessionInfo2 = testDataHandler.createSessionInfo(member2, trainer);

            Reservation reservation2 = Reservation.builder()
                    .reservationDates(List.of(reserveDate))
                    .trainer(trainer)
                    .member(member2)
                    .sessionInfo(sessionInfo2)
                    .name(member2.getName())
                    .dayOfWeek(reserveDate.getDayOfWeek())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation2 = reservationRepository.saveReservation(reservation2).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/waiting-members/"
                    + reserveDate, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.WaitingMember> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.WaitingMember.class);
                softly.assertThat(content).hasSize(2);
                softly.assertThat(content.get(0).reservationId()).isEqualTo(savedReservation1.getReservationId());
                softly.assertThat(content.get(1).reservationId()).isEqualTo(savedReservation2.getReservationId());

            });
        }

        @Test
        @DisplayName("트레이너 예약 상세 대기 목록 조회 실패 - reservationDate 정보 누락")
        void getReservationWithTrainerNoReservationDate() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member1 = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo1 = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reserveDate = LocalDateTime.now().plusDays(1);

            Reservation reservation1 = Reservation.builder()
                    .reservationDates(List.of(reserveDate))
                    .trainer(trainer)
                    .member(member1)
                    .sessionInfo(sessionInfo1)
                    .name(member1.getName())
                    .dayOfWeek(reserveDate.getDayOfWeek())
                    .status(RESERVATION_WAITING)
                    .build();

            reservationRepository.saveReservation(reservation1).orElseThrow();

            Member member2 = testDataHandler.createMember();
            SessionInfo sessionInfo2 = testDataHandler.createSessionInfo(member2, trainer);

            Reservation reservation2 = Reservation.builder()
                    .reservationDates(List.of(reserveDate))
                    .trainer(trainer)
                    .member(member2)
                    .sessionInfo(sessionInfo2)
                    .name(member2.getName())
                    .dayOfWeek(reserveDate.getDayOfWeek())
                    .status(RESERVATION_WAITING)
                    .build();

            reservationRepository.saveReservation(reservation2).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/waiting-members/", accessToken);

            // then
            assertSoftly(softly -> softly.assertThat(result.statusCode()).isEqualTo(500));
        }

        @Test
        @DisplayName("트레이너 예약 상세 대기 목록 조회 실패 - 예약 대기자 없음")
        void getReservationWithTrainerNoWaitingStatus() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member1 = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo1 = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reserveDate = LocalDateTime.now().plusDays(1);

            Reservation reservation1 = Reservation.builder()
                    .reservationDates(List.of(reserveDate))
                    .trainer(trainer)
                    .member(member1)
                    .sessionInfo(sessionInfo1)
                    .name(member1.getName())
                    .dayOfWeek(reserveDate.getDayOfWeek())
                    .status(RESERVATION_APPROVED)
                    .build();

            reservationRepository.saveReservation(reservation1).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/waiting-members/"
                    + reserveDate, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class))
                        .contains("이 날짜에 예약 대기자가 없습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.WaitingMember.class))
                        .isNull();
            });
        }

    }

    @Nested
    @DisplayName("예약 불가 설정 Integration TEST")
    class SetDisabledTimeIntegrationTest {
        @Test
        @DisplayName("예약 불가 설정을 하면 이전의 있던 예약들은 취소되며, 예약 불가 설정한 예약은 저장됩니다.")
        void setDisabledReservation() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(trainer)
                    .member(member)
                    .name(member.getName())
                    .dayOfWeek(reservationDate.getDayOfWeek())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.SetDisabledTime request = ReservationRequestDto.SetDisabledTime
                    .builder()
                    .date(requestDate)
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/availability/disable",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)
                        .reservationId()).isEqualTo(2L);
            });
        }

        @Test
        @DisplayName("예약 불가 설정 - 성공 ")
        void setDisabledReservationWithCancelSession() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(trainer)
                    .member(member)
                    .sessionInfo(sessionInfo)
                    .name(member.getName())
                    .dayOfWeek(reservationDate.getDayOfWeek())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            Session session = Session.builder()
                    .reservation(savedReservation)
                    .build();

            reservationRepository.saveSession(session);


            ReservationRequestDto.SetDisabledTime request = ReservationRequestDto.SetDisabledTime
                    .builder()
                    .date(reservationDate)
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/availability/disable",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)
                        .reservationId()).isEqualTo(2L);
                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(savedReservation.getReservationId(),
                        Notification.ReferenceType.RESERVATION_REQUEST);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(RESERVATION_REFUSE);
            });
        }

        @Test
        @DisplayName("예약 불가 설정한 시간에 취소할 예약이 없으면 예약 불가 설정한 예약만 저장됩니다.")
        void setOnlyDisabledReservation() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.SetDisabledTime request = ReservationRequestDto.SetDisabledTime
                    .builder()
                    .date(requestDate)
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/availability/disable",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)
                        .reservationId()).isEqualTo(1L);
            });
        }

        @Test
        @DisplayName("예약 불가 설정 취소 - 성공")
        void setDisabledReservationCancel() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();
            Member member = memberRepository.getMember(1L).orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Reservation reservation = Reservation.builder()
                    .trainer(trainer)
                    .member(member)
                    .reservationDates(List.of(LocalDateTime.now().plusDays(1)))
                    .status(DISABLED_TIME_RESERVATION)
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            LocalDateTime requestDate = LocalDateTime.now().plusDays(1);

            ReservationRequestDto.SetDisabledTime request = ReservationRequestDto.SetDisabledTime
                    .builder()
                    .date(requestDate)
                    .reservationId(savedReservation.getReservationId())
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/availability/disable",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)
                        .reservationId()).isEqualTo(1L);

                // 예약 불가 설정이 삭제되었는지 확인
                Optional<Reservation> deleteReservation = reservationRepository
                        .getReservation(savedReservation.getReservationId());

                softly.assertThat(deleteReservation.isPresent()).isFalse();
            });
        }

        @Test
        @DisplayName("예약 불가 설정 취소 - 실패 : 예약 불가 설정 상태가 아님")
        void setDisabledReservationCancelWithNotThatStatus() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();
            Member member = memberRepository.getMember(1L).orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Reservation reservation = Reservation.builder()
                    .trainer(trainer)
                    .member(member)
                    .reservationDates(List.of(LocalDateTime.now().plusDays(1)))
                    .status(RESERVATION_CANCEL_REQUEST)
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            LocalDateTime requestDate = LocalDateTime.now().plusDays(1);

            ReservationRequestDto.SetDisabledTime request = ReservationRequestDto.SetDisabledTime
                    .builder()
                    .date(requestDate)
                    .reservationId(savedReservation.getReservationId())
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/availability/disable",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("예약 불가 해지할 수 있는 상태가 아닙니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }
    }

    @Nested
    @DisplayName("직접 예약 Integration TEST")
    class CreateReservationIntegrationTest {

        @Test
        @DisplayName("트레이너가 직접 예약 성공")
        void createReservationWithTrainer() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .dates(List.of(requestDate))
                    .name("홍길동")
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH,
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                // 예약이 잘 됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_APPROVED.getName());

                // 세션이 잘 생성됐는지 확인
                Session session = reservationRepository.getSession(content.reservationId()).orElseThrow();
                softly.assertThat(session).isNotNull();
                softly.assertThat(session.getStatus()).isEqualTo(SESSION_WAITING);

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_REQUEST);
                softly.assertThat(notifications).isNotEmpty();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(RESERVATION_APPROVE);
            });
        }

        @Test
        @DisplayName("멤버가 직접 예약 성공")
        void createReservationWithMember() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .dates(List.of(requestDate))
                    .name("홍길동")
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH,
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약이 잘됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_WAITING.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notification = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_REQUEST);
                softly.assertThat(notification.get(0)).isNotNull();
                softly.assertThat(notification.get(0).getNotificationType()).isEqualTo(RESERVATION_REQUESTED);
            });
        }

        @Test
        @DisplayName("멤버가 직접 예약 성공 - 우선 예약 포함 2개 예약")
        void createReservationWithMemberAndPriority() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate1 = LocalDateTime.now().plusHours(3);
            LocalDateTime requestDate2 = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .dates(List.of(requestDate1, requestDate2))
                    .name("홍길동")
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH,
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약이 잘 됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_WAITING.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notification = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_REQUEST);
                softly.assertThat(notification.get(0)).isNotNull();
                softly.assertThat(notification.get(0).getNotificationType()).isEqualTo(RESERVATION_REQUESTED);
            });
        }

        @Test
        @DisplayName("직접 예약 실패 - trainerID 부재")
        void createReservationNoTrainerId() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .memberId(1L)
                    .dates(List.of(requestDate))
                    .name("홍길동")
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH,
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("트레이너 ID는 필수값 입니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Summary.class))
                        .isNull();
            });
        }

        @Test
        @DisplayName("직접 예약 실패 - memberID 부재")
        void createReservationNoMemberId() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .dates(List.of(requestDate))
                    .name("홍길동")
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH,
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("유저 ID는 필수값 입니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Summary.class))
                        .isNull();
            });
        }

        @Test
        @DisplayName("직접 예약 실패 - dates 부재")
        void createReservationNoDate() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .name("홍길동")
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH,
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("예약 요청 날짜는 비어있을 수 없습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Summary.class))
                        .isNull();
            });
        }

        @Test
        @DisplayName("직접 예약 실패 - name 부재")
        void createReservationNoName() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .dates(List.of(requestDate))
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH,
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("이름은 필수값 입니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Summary.class))
                        .isNull();
            });
        }
    }

    @Nested
    @DisplayName("고정 세션 예약 Integration TEST")
    class CreateFixedReservationIntegrationTest {
        @Test
        @DisplayName("트레이너가 고정 세션 예약 성공 - 고정 예약 1개, 초기 남은 세션 6, 예약 6개 생성")
        void createFixedReservationWithTrainer() {
            // given
            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();
            int originCount = sessionInfo.getRemainingCount();

            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .memberId(1L)
                    .name("홍길동")
                    .dates(List.of(requestDate))
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/fixed-reservations",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                // 예약이 잘 됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                List<ReservationResponseDto.Success> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.get(0).reservationId()).isEqualTo(1L);
                softly.assertThat(content.get(0).status()).isEqualTo(FIXED_RESERVATION.getName());

                // 세션이 잘 생성됐는지 확인
                Session session = reservationRepository.getSession(content.get(0).reservationId()).orElseThrow();
                softly.assertThat(session).isNotNull();
                softly.assertThat(session.getStatus()).isEqualTo(SESSION_WAITING);

                //총 세션 수만큼 예약이 됐는지 확인
                SessionInfo afterSessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();
                softly.assertThat(afterSessionInfo.getRemainingCount()).isEqualTo(0);

                List<Reservation> reservations = reservationRepository.getReservations();
                softly.assertThat(reservations.size()).isEqualTo(originCount);

            });
        }

        @Test
        @DisplayName("트레이너가 고정 세션 예약 성공 - 고정 예약 2개, 초기 남은 세션 6, 예약 6개 생성")
        void twoCreateFixedReservationWithTrainer() {
            // given
            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();
            int originCount = sessionInfo.getRemainingCount();

            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate1 = LocalDateTime.now().plusHours(1);
            LocalDateTime requestDate2 = LocalDateTime.now().plusDays(1).plusHours(1);

            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .memberId(1L)
                    .name("홍길동")
                    .dates(List.of(requestDate1, requestDate2))
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/fixed-reservations",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                // 예약이 잘 됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                List<ReservationResponseDto.Success> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.get(0).reservationId()).isEqualTo(1L);
                softly.assertThat(content.get(0).status()).isEqualTo(FIXED_RESERVATION.getName());
                softly.assertThat(content.get(1).reservationId()).isEqualTo(2L);
                softly.assertThat(content.get(1).status()).isEqualTo(FIXED_RESERVATION.getName());

                // 세션이 잘 생성됐는지 확인
                Session session1 = reservationRepository.getSession(content.get(0).reservationId()).orElseThrow();
                softly.assertThat(session1).isNotNull();
                softly.assertThat(session1.getStatus()).isEqualTo(SESSION_WAITING);

                Session session2 = reservationRepository.getSession(content.get(1).reservationId()).orElseThrow();
                softly.assertThat(session2).isNotNull();
                softly.assertThat(session2.getStatus()).isEqualTo(SESSION_WAITING);

                //총 세션 수만큼 예약이 됐는지 확인
                SessionInfo afterSessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();
                softly.assertThat(afterSessionInfo.getRemainingCount()).isEqualTo(0);

                List<Reservation> reservations = reservationRepository.getReservations();
                softly.assertThat(reservations.size()).isEqualTo(originCount);
            });
        }

        @Test
        @DisplayName("트레이너가 고정 세션 예약 성공 - 기존에 존재하던 예약 거절")
        void createFixedReservationWithCancelAlreadySaveReservation() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .memberId(1L)
                    .name("홍길동")
                    .dates(List.of(requestDate))
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation)
                    .orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/fixed-reservations",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                // 예약이 잘 됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                List<ReservationResponseDto.Success> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.get(0).reservationId()).isEqualTo(2L);
                softly.assertThat(content.get(0).status()).isEqualTo(FIXED_RESERVATION.getName());

                // 세션이 잘 생성됐는지 확인
                Session session = reservationRepository.getSession(content.get(0).reservationId()).orElseThrow();
                softly.assertThat(session).isNotNull();
                softly.assertThat(session.getStatus()).isEqualTo(SESSION_WAITING);

                // 기존 예약이 잘 취소됐는지 확인
                Reservation originReservation = reservationRepository
                        .getReservation(savedReservation.getReservationId())
                        .orElseThrow();

                softly.assertThat(originReservation.getStatus()).isEqualTo(RESERVATION_REFUSED);

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(originReservation.getReservationId(),
                        Notification.ReferenceType.RESERVATION_REQUEST);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(RESERVATION_REFUSE);


            });
        }

        @Test
        @DisplayName("트레이너가 고정 세션 예약 실패 - 멤버 ID 누락")
        void createFixedReservationWithNoMemberId() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .name("홍길동")
                    .dates(List.of(requestDate))
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/fixed-reservations",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("유저 ID는 필수값 입니다.");
                softly.assertThat(result.body().jsonPath().getList("data", ReservationResponseDto.Success.class))
                        .isEmpty();
            });
        }

        @Test
        @DisplayName("트레이너가 고정 세션 예약 실패 - 이름 누락")
        void createFixedReservationWithNoName() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .memberId(1L)
                    .dates(List.of(requestDate))
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/fixed-reservations",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("이름은 필수값 입니다.");
                softly.assertThat(result.body().jsonPath().getList("data", ReservationResponseDto.Success.class))
                        .isEmpty();
            });
        }

        @Test
        @DisplayName("트레이너가 고정 세션 예약 실패 - date 누락")
        void createFixedReservationWithNoDate() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());


            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .memberId(1L)
                    .name("홍길동")
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/fixed-reservations",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("예약 요청 날짜는 비어있을 수 없습니다.");
                softly.assertThat(result.body().jsonPath().getList("data", ReservationResponseDto.Success.class))
                        .isEmpty();
            });
        }

    }

    @Nested
    @DisplayName("예약 스케줄러 Integration TEST")
    class ReservationSchedulerIntegrationTest {
        @Test
        @DisplayName("스케줄 고정 세션 예약 : 성공")
        void scheduledCreateFixedReservation() {
            // given
            LocalDateTime requestDate = LocalDateTime.now().plusSeconds(1);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .sessionInfo(SessionInfo.builder().SessionInfoId(1L).build())
                    .status(FIXED_RESERVATION)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation);

            // when
            reservationScheduler.createFixedReservations();

            // then
            assertSoftly(softly -> {
                // 예약이 잘 됐는지 확인
                List<Reservation> reservations = reservationRepository.getReservations();
                softly.assertThat(reservations).hasSize(1);
                softly.assertThat(reservations.get(0).getReservationId()).isEqualTo(1L);
                softly.assertThat(reservations.get(0).getStatus()).isEqualTo(FIXED_RESERVATION);

                // outbox 데이터 잘 쌓였나 확인
                List<Outbox> outboxes = outboxRepository.getOutboxes();
                softly.assertThat(outboxes).hasSize(1);
                softly.assertThat(outboxes.get(0).getOutboxId()).isEqualTo(1L);
                softly.assertThat(outboxes.get(0).getAggregateType()).isEqualTo(Outbox.AggregateType.RESERVATION);
                softly.assertThat(outboxes.get(0).getEventType()).isEqualTo(Outbox.EventType.CREATE_FIXED_RESERVATION);
                softly.assertThat(outboxes.get(0).getEventStatus()).isEqualTo(Outbox.EventStatus.SEND_SUCCESS);
                softly.assertThat(outboxes.get(0).getRetryCount()).isEqualTo(0);
            });
        }

        @Test
        @DisplayName("세션 리마인드 알림 전송 - 성공")
        void sessionReminder() {
            // given
            LocalDateTime reservationDate = LocalDateTime.now();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .confirmDate(reservationDate)
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            Session savedSession = reservationRepository.saveSession(session).orElseThrow();

            // when
            reservationScheduler.sessionReminder();

            // then
            assertSoftly(softly -> {
                //세션 리마인더 알림이 잘 생성되었는지 확인
                List<Notification> notifications = notificationRepository.getNotification(savedSession.getSessionId(),
                        Notification.ReferenceType.SESSION);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(
                        SESSION_REMINDER);
            });
        }
    }

    @Nested
    @DisplayName("예약 취소 Integration TEST")
    class CancelReservationIntegrationTest {
        @Test
        @DisplayName("트레이너가 예약 취소 성공 - 확정된 예약")
        void cancelReservationWithTrainer() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelDate(requestDate)
                    .cancelReason("개인 사정")
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약이 잘 취소 됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_CANCELLED.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CANCEL);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(RESERVATION_CANCEL);
            });

        }

        @Test
        @DisplayName("멤버가 예약 취소 요청 성공 - 확정된 예약")
        void cancelRequestReservationWithMember() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelDate(requestDate)
                    .cancelReason("개인 사정")
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .name("길동")
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약이 잘 취소 됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_CANCEL_REQUEST.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CANCEL);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(RESERVATION_CANCEL);
            });
        }

        @Test
        @DisplayName("멤버가 예약 취소 성공 - 대기 중인 예약")
        void cancelReservationWithMember() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelDate(requestDate)
                    .cancelReason("개인 사정")
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .name("길동")
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약이 잘 취소 됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_CANCELLED.getName());
            });
        }

        @Test
        @DisplayName("트레이너가 예약 취소 실패 - 예약 사유 부재")
        void cancelReservationWithNoCancelReason() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusDays(1);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelDate(requestDate)
                    .cancelReason("")
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("취소 사유는 필수값 입니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });

        }

        @Test
        @DisplayName("트레이너가 예약 취소 실패 - 없는 예약 취소 요청")
        void cancelReservationWithNoExistReservation() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelReason("개인 사유")
                    .cancelDate(LocalDateTime.now().plusHours(3))
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("예약 정보를 찾을 수 없습니다. [reservationId: 1]");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });

        }

        @Test
        @DisplayName("트레이너가 예약 취소 실패 - 잘못된 예약 날짜 취소 시도")
        void cancelReservationWithNoExistReservationDate() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusDays(1);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelDate(requestDate.plusHours(2))
                    .cancelReason("개인 사유")
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("예약 날짜를 찾지 못하였습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });

        }

        @Test
        @DisplayName("트레이너가 예약 취소 실패 - 이미 취소된 예약 취소 시도")
        void cancelReservationWithAlreadyCancel() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelReason("개인 사유")
                    .cancelDate(requestDate)
                    .build();

            // 취소된 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_CANCELLED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("이미 예약이 취소되었습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });

        }

        @Test
        @DisplayName("트레이너가 예약 취소 실패 - 예약 취소 불가 상태 예약 취소 시도")
        void cancelReservationWithCancelNotAllowed() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelReason("개인 사유")
                    .cancelDate(requestDate)
                    .build();

            // 예약 승인이 거절된 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_REFUSED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("예약 취소를 할 수 없는 상태입니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });

        }

        @Test
        @DisplayName("멤버가 예약 취소 요청 실패 - 당일 예약 취소 요청")
        void cancelReservationWithRequestToday() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelReason("개인 사유")
                    .cancelDate(requestDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(requestDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("당일 예약 취소 요청은 불가합니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });

        }

    }

    @Nested
    @DisplayName("예약 승인 Integration TEST")
    class ApproveReservationIntegrationTest {
        @Test
        @DisplayName("예약 승인 성공 - 동시간 다른 예약들이 없는 경우")
        void approveReservationWithTrainer() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            ReservationRequestDto.Approve request = ReservationRequestDto.Approve.builder()
                    .memberId(1L)
                    .reservationDate(reservationDate)
                    .build();


            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약이 잘 승인됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_APPROVED.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_REQUEST);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(RESERVATION_APPROVE);
            });
        }

        @Test
        @DisplayName("예약 승인 성공 - 동시간 다른 예약들은 거절 됨")
        void approveReservationWithOtherReservationRefused() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            ReservationRequestDto.Approve request = ReservationRequestDto.Approve.builder()
                    .memberId(1L)
                    .reservationDate(reservationDate)
                    .build();


            // 예약 생성
            Reservation reservation1 = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation1).orElseThrow();


            // 다른 멤버의 예약 생성
            Member member = testDataHandler.createMember();
            testDataHandler.createTokenInfo(member);

            Reservation reservation2 = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(member)
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation otherReservation = reservationRepository.saveReservation(reservation2).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약이 잘 승인됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_APPROVED.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        UserRole.MEMBER, Notification.ReferenceType.RESERVATION_REQUEST);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(RESERVATION_APPROVE);

                // 다른 예약 상태가 거절 상태인지 확인
                Reservation refuseReservation = reservationRepository
                        .getReservation(otherReservation.getReservationId()).orElseThrow();
                softly.assertThat(refuseReservation.getStatus()).isEqualTo(RESERVATION_REFUSED);

                // 다른 예약 알림 내용이 거절 내용인지 확인
                List<Notification> notifications2 = notificationRepository.getNotification(refuseReservation.getReservationId(),
                        UserRole.MEMBER, Notification.ReferenceType.RESERVATION_REQUEST);
                softly.assertThat(notifications2.get(0)).isNotNull();
                softly.assertThat(notifications2.get(0).getNotificationType()).isEqualTo(RESERVATION_REFUSE);


            });
        }

        @Test
        @DisplayName("예약 승인 실패 - 멤버가 API 호출")
        void approveReservationWithMember() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            ReservationRequestDto.Approve request = ReservationRequestDto.Approve.builder()
                    .memberId(1L)
                    .reservationDate(reservationDate)
                    .build();


            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(403);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("접근 권한이 없습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();

            });
        }

        @Test
        @DisplayName("예약 승인 실패 - 멤버 ID 부재 ")
        void approveReservationNoMemberId() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            ReservationRequestDto.Approve request = ReservationRequestDto.Approve.builder()
                    .reservationDate(reservationDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("유저 ID는 필수값 입니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

        @Test
        @DisplayName("예약 승인 실패 - 예약 날짜 부재 ")
        void approveReservationNoReservationDate() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            ReservationRequestDto.Approve request = ReservationRequestDto.Approve.builder()
                    .memberId(1L)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("요청 날짜는 비어있을 수 없습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

        @Test
        @DisplayName("예약 승인 실패 - 이전 날짜 요청 ")
        void approveReservationBeforeToday() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            ReservationRequestDto.Approve request = ReservationRequestDto.Approve.builder()
                    .memberId(1L)
                    .reservationDate(LocalDateTime.now().minusSeconds(1))
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("현재 날짜보다 이전일 수 없습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }


        @Test
        @DisplayName("예약 승인 실패 - 이미 승인 된 예약 ")
        void approveReservationAlreadyApprovedReservation() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            ReservationRequestDto.Approve request = ReservationRequestDto.Approve.builder()
                    .memberId(1L)
                    .reservationDate(reservationDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("이미 예약이 승인 되었습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

    }

    @Nested
    @DisplayName("진행한 PT 처리 Integration TEST")
    class CompleteSessionIntegrationTest {
        @Test
        @DisplayName("트레이너의 PT 처리 성공 - 회원이 PT 참석한 경우")
        void completeSessionWithJoinSession() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .memberId(1L)
                    .isJoin(true)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/sessions/complete".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약이 잘 승인됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.SuccessSession content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.SuccessSession.class);

                softly.assertThat(content.sessionId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(Notification.NotificationType.SESSION_COMPLETED.getName());

                // 세션 차감 알림이 잘 생성됐는지 확인
                PersonalDetail memberDetail = personalDetailRepository.getMemberDetail(1L).orElseThrow();
                Notification notification = notificationRepository.getNotification(memberDetail.getPersonalDetailId(),
                        SESSION_DEDUCTED);
                softly.assertThat(notification).isNotNull();
                softly.assertThat(notification.getNotificationType()).isEqualTo(SESSION_DEDUCTED);

            });
        }

        @Test
        @DisplayName("트레이너의 PT 처리 성공 - 회원이 PT 참석하지 않은 경우")
        void completeSessionWithNotJoinSession() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .memberId(1L)
                    .isJoin(false)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/sessions/complete".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약이 잘 승인됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.SuccessSession content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.SuccessSession.class);

                softly.assertThat(content.sessionId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(SESSION_NOT_ATTEND.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.sessionId(),
                        UserRole.MEMBER, Notification.ReferenceType.SESSION);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(SESSION_DEDUCTED);
            });
        }

        @Test
        @DisplayName("트레이너의 PT 처리 실패 - 세션 정보 없음")
        void completeSessionWithNoSessionInfo() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .memberId(1L)
                    .isJoin(true)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();


            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/sessions/complete".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(404);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("세션 정보를 찾지 못하였습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

        @Test
        @DisplayName("트레이너의 PT 처리 실패 - 이미 세션 완료")
        void completeSessionWithAlreadySessionCompleted() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .memberId(1L)
                    .isJoin(true)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성

            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_COMPLETED)
                    .build();

            reservationRepository.saveSession(session);


            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/sessions/complete".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("이미 세션이 종료되었습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

        @Test
        @DisplayName("트레이너의 PT 처리 실패 - 이미 완료된 예약")
        void completeSessionAlreadyReservationCompleted() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .memberId(1L)
                    .isJoin(true)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_COMPLETED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/sessions/complete".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("이미 예약이 완료되었습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }


        @Test
        @DisplayName("트레이너의 PT 처리 실패 - 다른 예약 정보 수정")
        void completeSessionCompleteOtherReservationInfo() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .memberId(1L)
                    .isJoin(true)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .trainer(Trainer.builder().trainerId(2L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/sessions/complete".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("다른 사람의 예약을 완료시킬 수 없습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }
    }

    @Nested
    @DisplayName("예약 변경 요청 Integration TEST")
    class ChangeRequestReservationIntegrationTest {

        @Test
        @DisplayName("멤버의 예약 변경 요청 성공 - 예약이 확정 상태인 경우")
        void changeReservationWithMember() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-request".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약 변경 요청이 잘 되었는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_CHANGE_REQUEST.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CHANGE);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType())
                        .isEqualTo(Notification.NotificationType.RESERVATION_CHANGE_REQUEST);
            });
        }

        @Test
        @DisplayName("멤버의 예약 변경 요청 성공 - 예약 대기 상태인 경우")
        void changeReservationWithMemberAndWaitingStatus() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-request".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약 변경 요청이 잘 되었는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_CHANGE_REQUEST.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CHANGE);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType())
                        .isEqualTo(Notification.NotificationType.RESERVATION_CHANGE_REQUEST);
            });
        }

        @Test
        @DisplayName("멤버의 예약 변경 요청 성공 - 예약 대기 상태이면서 2개의 예약 날짜")
        void changeReservationWithMemberAndWaitingStatusTwoDates() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .name("길동")
                    .reservationDates(List.of(reservationDate, reservationDate.plusHours(2)))
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-request".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약 변경 요청이 잘 되었는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_CHANGE_REQUEST.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CHANGE);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType())
                        .isEqualTo(Notification.NotificationType.RESERVATION_CHANGE_REQUEST);
            });
        }

        @Test
        @DisplayName("멤버의 예약 변경 요청 실패 - 예약 변경 요청할 수 있는 상태가 아님")
        void changeReservationWithMemberNotAllowChangeRequestStatus() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_REFUSED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-request".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("예약 변경을 요청할 수 없는 상태입니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

        @Test
        @DisplayName("예약 변경 요청 실패 - 요청 예약 변경 날짜가 실제 예약 날짜랑 다름")
        void changeReservationWithMemberNotEqualReservationDate() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate.plusHours(1)))
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-request".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(404);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("예약 날짜를 찾지 못하였습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

        @Test
        @DisplayName("멤버의 예약 변경 요청 실패 - 예약 변경 요청할 수 있는 시간이 아님")
        void changeReservationWithMemberNotAllowChangeRequestHours() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-request".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("현재시간보다 2시간 이후부터 변경 가능합니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

    }

    @Nested
    @DisplayName("고정 예약 변경 Integration TEST")
    class ChangeFixedReservationIntegrationTest {
        @Test
        @DisplayName("트레이너의 고정 예약 변경 - 성공")
        void changeFixedReservation() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(FIXED_RESERVATION)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/fixed-change-request".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                // 고정 예약 변경이 잘 되었는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(FIXED_RESERVATION.getName());

            });
        }

        @Test
        @DisplayName("트레이너의 고정 예약 변경 요청 - 실패: 고정 예약 상태가 아님")
        void changeFixedReservationWithNoFixedStatus() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_APPROVED)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/fixed-change-request".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("고정 예약 상태가 아닙니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();

            });
        }

        @Test
        @DisplayName("트레이너의 고정 예약 변경 요청 - 실패: 변경하고자 하는 날짜에 이미 예약이 있음")
        void changeFixedReservationAlreadyExistedReservation() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationDate = now.plusDays(1);
            LocalDateTime changeRequestDate = now.plusDays(2);

            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            // 예약 생성
            Reservation reservation1 = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(FIXED_RESERVATION)
                    .createdAt(now.plusSeconds(2))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation1).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // 다른 예약 하나 더 생성
            Reservation reservation2 = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(changeRequestDate))
                    .confirmDate(changeRequestDate)
                    .status(RESERVATION_APPROVED)
                    .createdAt(now.plusSeconds(2))
                    .build();

            Reservation savedReservation2 = reservationRepository.saveReservation(reservation2).orElseThrow();

            Session session2 = Session.builder()
                    .reservation(savedReservation2)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session2);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/fixed-change-request".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(409);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("해당 날짜에 확정된 예약이 존재합니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }
    }

    @Nested
    @DisplayName("예약 변경 승인 Integration TEST")
    class ChangeApproveReservationIntegrationTest {
        @Test
        @DisplayName("멤버의 예약 변경 승인 성공 - 예약 변경 요청 승인인 경우")
        void changeReqeustReservationWithApprove() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            ReservationRequestDto.ChangeApproval request = ReservationRequestDto.ChangeApproval
                    .builder()
                    .memberId(1L)
                    .isApprove(true)
                    .approveDate(reservationDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_CHANGE_REQUEST)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약 변경 승인이 잘 되었는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_APPROVED.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CHANGE);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(
                        Notification.NotificationType.RESERVATION_CHANGE_REQUEST_APPROVED);
            });
        }

        @Test
        @DisplayName("멤버의 예약 변경 승인 성공 - 예약 변경 요청 거절인 경우")
        void changeReqeustReservationWithRefuse() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            ReservationRequestDto.ChangeApproval request = ReservationRequestDto.ChangeApproval
                    .builder()
                    .memberId(1L)
                    .isApprove(false)
                    .approveDate(reservationDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_CHANGE_REQUEST)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약 변경 거절이 잘 되었는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(Reservation.Status.RESERVATION_CHANGE_REQUEST_REFUSED.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CHANGE);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(
                        Notification.NotificationType.RESERVATION_CHANGE_REQUEST_REFUSED);
            });
        }

        @Test
        @DisplayName("멤버의 예약 변경 승인 성공 - 같은 시간 대 다른 예약 대기 거절")
        void changeReqeustReservationWithRefuseOtherReservations() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            ReservationRequestDto.ChangeApproval request = ReservationRequestDto.ChangeApproval
                    .builder()
                    .memberId(1L)
                    .isApprove(true)
                    .approveDate(reservationDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_CHANGE_REQUEST)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation reservation1 = reservationRepository.saveReservation(reservation).orElseThrow();

            Member member2 = testDataHandler.createMember("하하");
            testDataHandler.createTokenInfo(member2);

            // 다른 예약 대기 상태 예약 생성
            Reservation reservation2 = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(member2)
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation savedReservation2 = reservationRepository.saveReservation(reservation2).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약 변경 거절이 잘 되었는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_APPROVED.getName());

                // 세션도 잘 생성되었는지 확인
                Session session = reservationRepository.getSession(reservation1.getReservationId())
                        .orElseThrow();

                softly.assertThat(session.getReservation().getReservationId()).isEqualTo(
                        reservation1.getReservationId());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CHANGE);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(
                        Notification.NotificationType.RESERVATION_CHANGE_REQUEST_APPROVED);

                // 거절 알림이 잘 생성됐는지 확인
                List<Notification> notifications2 = notificationRepository.getNotification(savedReservation2.getReservationId(),
                        Notification.ReferenceType.RESERVATION_REQUEST);
                softly.assertThat(notifications2.get(0)).isNotNull();
                softly.assertThat(notifications2.get(0).getNotificationType()).isEqualTo(RESERVATION_REFUSE);
            });
        }

        @Test
        @DisplayName("멤버의 예약 변경 승인 실패 - 예약 변경 요청 상태가 아님")
        void changeApproveReservationNotChangeRequestStatus() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            ReservationRequestDto.ChangeApproval request = ReservationRequestDto.ChangeApproval
                    .builder()
                    .memberId(1L)
                    .isApprove(true)
                    .approveDate(reservationDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("예약 변경 승인을 할 수 있는 상태가 아닙니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

        @Test
        @DisplayName("멤버의 예약 변경 승인 실패 - 다른 멤버의 예약 변경 수정 시도")
        void changeApproveReservationTryOtherMemberId() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            ReservationRequestDto.ChangeApproval request = ReservationRequestDto.ChangeApproval
                    .builder()
                    .memberId(2L)
                    .isApprove(true)
                    .approveDate(reservationDate)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_WAITING)
                    .createdAt(LocalDateTime.now().plusSeconds(2))
                    .build();

            reservationRepository.saveReservation(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/change-approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(404);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("잘못된 멤버의 예약을 변경하려고 합니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

    }

    @Nested
    @DisplayName("예약 취소 승인 Integration TEST")
    class CancelApproveReservationIntegrationTest {
        @Test
        @DisplayName("멤버의 예약 취소 승인 성공 - 예약 취소 요청 승인인 경우")
        void cancelApproveReservationWithApprove() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.CancelApproval request = ReservationRequestDto.CancelApproval
                    .builder()
                    .memberId(1L)
                    .isApprove(true)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
                    .status(RESERVATION_CANCEL_REQUEST)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel-approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약 취소 승인이 잘 되었는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_CANCELLED.getName());

                //세션이 잘 취소됐는지 확인
                Session cancelledSession = reservationRepository.getSession(content.reservationId()).orElseThrow();
                softly.assertThat(cancelledSession.getStatus()).isEqualTo(SESSION_CANCELLED);


                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CANCEL);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(
                        Notification.NotificationType.RESERVATION_CANCEL_REQUEST_APPROVED);
            });
        }

        @Test
        @DisplayName("멤버의 예약 취소 승인 성공 - 예약 취소 요청 거절인 경우")
        void cancelApproveReservationWithRefuse() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.CancelApproval request = ReservationRequestDto.CancelApproval
                    .builder()
                    .memberId(1L)
                    .isApprove(false)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
                    .status(RESERVATION_CANCEL_REQUEST)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel-approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                //예약 취소 승인이 잘 되었는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                ReservationResponseDto.Success content = result.body().jsonPath()
                        .getObject("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.reservationId()).isEqualTo(1L);
                softly.assertThat(content.status()).isEqualTo(RESERVATION_CANCEL_REQUEST_REFUSED.getName());

                // 알림이 잘 생성됐는지 확인
                List<Notification> notifications = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION_CANCEL);
                softly.assertThat(notifications.get(0)).isNotNull();
                softly.assertThat(notifications.get(0).getNotificationType()).isEqualTo(
                        Notification.NotificationType.RESERVATION_CANCEL_REQUEST_REFUSED);
            });
        }

        @Test
        @DisplayName("멤버의 예약 취소 승인 실패 - 예약 취소 요청 상태가 아님")
        void cancelApproveReservationNotChangeRequestStatus() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.CancelApproval request = ReservationRequestDto.CancelApproval
                    .builder()
                    .memberId(1L)
                    .isApprove(true)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
                    .status(RESERVATION_CHANGE_REQUEST)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel-approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("예약 취소 승인을 할 수 있는 상태가 아닙니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }

        @Test
        @DisplayName("멤버의 예약 취소 승인 실패 - 다른 멤버의 예약 변경 수정 시도")
        void cancelApproveReservationTryOtherMemberId() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            ReservationRequestDto.CancelApproval request = ReservationRequestDto.CancelApproval
                    .builder()
                    .memberId(2L)
                    .isApprove(true)
                    .build();

            // 예약 생성
            Reservation reservation = Reservation.builder()
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
                    .status(RESERVATION_CANCEL_REQUEST)
                    .createdAt(LocalDateTime.now().plusSeconds(3))
                    .build();

            Reservation savedReservation = reservationRepository.saveReservation(reservation).orElseThrow();

            // 세션 생성
            Session session = Session.builder()
                    .reservation(savedReservation)
                    .status(SESSION_WAITING)
                    .build();

            reservationRepository.saveSession(session);

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/%s/cancel-approve".formatted(1),
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(404);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("잘못된 멤버의 예약을 변경하려고 합니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }
    }

    @Nested
    @DisplayName("고정 예약 해지 Integration TEST")
    class ReleaseFixedReservationIntegrationTest {
        @Test
        @DisplayName("고정 예약 해지 - 성공: 이전 고정 예약 모두 취소")
        void releaseFixedReservation() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1).orElseThrow();
            int originCount = sessionInfo.getRemainingCount();

            int day = 1;
            LocalDateTime baseDate = LocalDateTime.now().plusDays(day);
            for (int i = 1; i <= 4; i++) {
                // 예약 생성
                LocalDateTime reservationDate = baseDate.plusDays(day * i * 7);
                Reservation reservationEntity = Reservation.builder()
                        .trainer(Trainer.builder().trainerId(1L).build())
                        .member(Member.builder().memberId(1L).build())
                        .reservationDates(List.of(reservationDate))
                        .confirmDate(reservationDate)
                        .status(FIXED_RESERVATION)
                        .createdAt(LocalDateTime.now().plusSeconds(3))
                        .build();
                Reservation savedReservation = reservationRepository.saveReservation(reservationEntity).orElseThrow();

                // 세션 생성
                Session session = Session.builder()
                        .reservation(savedReservation)
                        .status(SESSION_WAITING)
                        .build();

                reservationRepository.saveSession(session);
            }

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/fixed-reservations/%s/release"
                    .formatted(1), accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);

                List<ReservationResponseDto.Success> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Success.class);

                content.forEach(r -> {
                    softly.assertThat(r.status()).isEqualTo(RESERVATION_CANCELLED.getName());

                    //세션이 취소되었는지 확인
                    Session session = reservationRepository.getSession(r.reservationId()).orElseThrow();
                    softly.assertThat(session.getStatus()).isEqualTo(SESSION_CANCELLED);
                });

                softly.assertThat(content.size()).isEqualTo(4);

                // 다시 세션 수 복구되었는지 확인
                SessionInfo afterSessionInfo = sessionInfoRepository.getSessionInfo(1).orElseThrow();
                softly.assertThat(originCount + 4).isEqualTo(afterSessionInfo.getRemainingCount());
            });
        }
    }
}
