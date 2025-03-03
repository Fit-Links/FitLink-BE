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
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationRepository;
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
import spring.fitlinkbe.support.security.AuthTokenProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static spring.fitlinkbe.domain.notification.Notification.NotificationType.*;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_APPROVED;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_WAITING;

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
    TestDataHandler testDataHandler;


    @BeforeEach
    void setUp() {
        testDataHandler.settingUserInfo();
        testDataHandler.settingSessionInfo();
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

            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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
                    .build();

            reservationRepository.reserveSession(reservation);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.GetList> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.GetList.class);
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
                    .build();

            reservationRepository.reserveSession(reservation);

            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.GetList> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.GetList.class);
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

            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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

            reservationRepository.reserveSession(reservation);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.GetList> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.GetList.class);
                softly.assertThat(content.size()).isSameAs(0);
            });
        }

        @Test
        @DisplayName("멤버는 1달 안에 예약 목록이 있을 경우 목록을 조회한다.")
        void getReservationsWithMemberIn1Month() {
            // given
            Map<String, String> params = new HashMap<>();
            params.put("date", LocalDate.now().toString());

            PersonalDetail personalDetails = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            Member member = memberRepository.getMember(1L).orElseThrow();

            SessionInfo sessionInfo = sessionInfoRepository.getSessionInfo(1L).orElseThrow();

            LocalDateTime reqeustDate = LocalDateTime.now().plusMonths(1).minusDays(1).minusSeconds(1);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reqeustDate))
                    .trainer(trainer)
                    .member(member)
                    .sessionInfo(sessionInfo)
                    .name(member.getName())
                    .dayOfWeek(reqeustDate.getDayOfWeek())
                    .build();

            reservationRepository.reserveSession(reservation);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.GetList> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.GetList.class);
                softly.assertThat(content.size()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("멤버는 1달하고 하루 뒤에 예약 목록을 조회할 경우 조회 하지 못한다.")
        void getReservationsWithMemberIn1MonthWithEmptyDate() {
            // given
            Map<String, String> params = new HashMap<>();
            params.put("date", LocalDate.now().toString());

            PersonalDetail personalDetails = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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

            reservationRepository.reserveSession(reservation);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.GetList> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.GetList.class);
                softly.assertThat(content.size()).isEqualTo(0);
            });
        }

    }

    @Nested
    @DisplayName("예약 상세 조회 Integration TEST")
    class GetReservationIntegrationTest {

        @Test
        @DisplayName("트레이너는 예약 상세 목록을 조회한다.")
        void getReservationWithTrainer() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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

            Reservation savedReservation = reservationRepository.reserveSession(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/"
                    + savedReservation.getReservationId(), accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.GetDetail.class)
                                .reservationId())
                        .isEqualTo(1L);
            });
        }

        @Test
        @DisplayName("멤버가 예약 상세 목록을 조회한다.")
        void getReservationWithMember() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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

            Reservation savedReservation = reservationRepository.reserveSession(reservation).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/"
                    + savedReservation.getReservationId(), accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.GetDetail.class)
                                .reservationId())
                        .isEqualTo(1L);
            });
        }

        @Test
        @DisplayName("예약이 승낙이 됐다면, 세션 정보도 같이 상세 목록에 조회한다.")
        void getReservationWithApproved() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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

            Reservation savedReservation = reservationRepository.reserveSession(reservation).orElseThrow();

            Session session = Session.builder()
                    .reservation(savedReservation)
                    .build();

            reservationRepository.createSession(session);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/"
                    + savedReservation.getReservationId(), accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.GetDetail.class)
                                .sessionId())
                        .isEqualTo(1L);
            });
        }
    }

    @Nested
    @DisplayName("예약 상세 대기 목록 조회 Integration TEST")
    class GetWaitingMembersIntegrationTest {
        @Test
        @DisplayName("트레이너 예약 상세 대기 목록 조회 성공")
        void getReservationWaitingMembersWithTrainer() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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

            Reservation savedReservation1 = reservationRepository.reserveSession(reservation1).orElseThrow();

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

            Reservation savedReservation2 = reservationRepository.reserveSession(reservation2).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/waiting-members/"
                    + reserveDate, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<ReservationResponseDto.GetWaitingMember> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.GetWaitingMember.class);
                softly.assertThat(content).hasSize(2);
                softly.assertThat(content.get(0).reservationId()).isEqualTo(savedReservation1.getReservationId());
                softly.assertThat(content.get(1).reservationId()).isEqualTo(savedReservation2.getReservationId());

            });
        }

        @Test
        @DisplayName("트레이너 예약 상세 대기 목록 조회 실패 - reservationDate 정보 누락")
        void getReservationWithTrainerNoReservationDate() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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

            reservationRepository.reserveSession(reservation1).orElseThrow();

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

            reservationRepository.reserveSession(reservation2).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/waiting-members/", accessToken);

            // then
            assertSoftly(softly -> softly.assertThat(result.statusCode()).isEqualTo(500));
        }

        @Test
        @DisplayName("트레이너 예약 상세 대기 목록 조회 실패 - 예약 대기자 없음")
        void getReservationWithTrainerNoWaitingStatus() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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

            reservationRepository.reserveSession(reservation1).orElseThrow();

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/waiting-members/"
                    + reserveDate, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class))
                        .contains("이 날짜에 예약 대기자가 없습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.GetWaitingMember.class))
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
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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
                    .build();

            reservationRepository.reserveSession(reservation).orElseThrow();

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
        @DisplayName("예약 불가 설정을 하면 이전의 확정된 예약들과 함께 세션도 취소되며, 예약 불가 설정한 예약은 저장됩니다.")
        void setDisabledReservationWithCancelSession() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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
                    .build();

            Reservation savedReservation = reservationRepository.reserveSession(reservation).orElseThrow();

            Session session = Session.builder()
                    .reservation(savedReservation)
                    .build();

            reservationRepository.createSession(session);

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
                // 알람이 잘 생성됐는지 확인
                Notification notification = notificationRepository.getNotification(savedReservation.getReservationId(),
                        Notification.ReferenceType.RESERVATION);
                softly.assertThat(notification).isNotNull();
                softly.assertThat(notification.getNotificationType()).isEqualTo(RESERVATION_CANCEL);
            });
        }

        @Test
        @DisplayName("예약 불가 설정한 시간에 취소할 예약이 없으면 예약 불가 설정한 예약만 저장됩니다.")
        void setOnlyDisabledReservation() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

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
    }

    @Nested
    @DisplayName("직접 예약 Integration TEST")
    class ReserveSessionIntegrationTest {

        @Test
        @DisplayName("트레이너가 직접 예약 성공")
        void reserveSessionWithTrainer() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
                softly.assertThat(content.status()).isEqualTo(RESERVATION_APPROVED);

                // 세션이 잘 생성됐는지 확인
                Session session = reservationRepository.getSession(content.reservationId()).orElseThrow();
                softly.assertThat(session).isNotNull();
                softly.assertThat(session.getStatus()).isEqualTo(Session.Status.SESSION_WAITING);

                // 알람이 잘 생성됐는지 확인
                Notification notification = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION);
                softly.assertThat(notification).isNotNull();
                softly.assertThat(notification.getNotificationType()).isEqualTo(RESERVATION_APPROVE);
            });
        }

        @Test
        @DisplayName("멤버가 직접 예약 성공")
        void reserveSessionWithMember() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
                softly.assertThat(content.status()).isEqualTo(RESERVATION_WAITING);

                // 알람이 잘 생성됐는지 확인
                Notification notification = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION);
                softly.assertThat(notification).isNotNull();
                softly.assertThat(notification.getNotificationType()).isEqualTo(RESERVATION_REQUESTED);
            });
        }

        @Test
        @DisplayName("멤버가 직접 예약 성공 - 우선 예약 포함 2개 예약")
        void reserveSessionWithMemberAndPriority() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

            LocalDateTime requestDate1 = LocalDateTime.now().plusHours(3);
            LocalDateTime requestDate2 = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
                softly.assertThat(content.status()).isEqualTo(RESERVATION_WAITING);

                // 알람이 잘 생성됐는지 확인
                Notification notification = notificationRepository.getNotification(content.reservationId(),
                        Notification.ReferenceType.RESERVATION);
                softly.assertThat(notification).isNotNull();
                softly.assertThat(notification.getNotificationType()).isEqualTo(RESERVATION_REQUESTED);
            });
        }

        @Test
        @DisplayName("직접 예약 실패 - trainerID 부재")
        void reserveSessionNoTrainerId() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.GetList.class))
                        .isNull();
            });
        }

        @Test
        @DisplayName("직접 예약 실패 - memberID 부재")
        void reserveSessionNoMemberId() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.GetList.class))
                        .isNull();
            });
        }

        @Test
        @DisplayName("직접 예약 실패 - dates 부재")
        void reserveSessionNoDate() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.GetList.class))
                        .isNull();
            });
        }

        @Test
        @DisplayName("직접 예약 실패 - name 부재")
        void reserveSessionNoName() {
            // given
            PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetails.getPersonalDetailId());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.GetList.class))
                        .isNull();
            });
        }
    }
}
