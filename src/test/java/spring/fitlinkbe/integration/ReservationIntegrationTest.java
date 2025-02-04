package spring.fitlinkbe.integration;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationRepository;
import spring.fitlinkbe.domain.trainer.DayOff;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationDto;
import spring.fitlinkbe.support.security.AuthTokenProvider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

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
    MemberRepository memberRepository;

    @Autowired
    TestDataHandler testDataHandler;


    @BeforeEach
    void setUp() {
        testDataHandler.settingUserInfo();
    }

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

        Member member = memberRepository.getMember(1L).orElseThrow();

        LocalDateTime reqeustDate = LocalDateTime.now().plusWeeks(2).minusDays(1).minusSeconds(1);

        Reservation reservation = Reservation.builder()
                .reservationDate(reqeustDate)
                .trainerId(1L)
                .memberId(1L)
                .name(member.getName())
                .dayOfWeek(reqeustDate.getDayOfWeek())
                .priority(0)
                .build();

        reservationRepository.saveReservation(reservation);

        // when
        ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.statusCode()).isEqualTo(200);
            List<ReservationDto.Response> content = result.body().jsonPath()
                    .getList("data", ReservationDto.Response.class);
            softly.assertThat(content.size()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("트레이너의 휴무일도 예약 목록에 같이 나온다.")
    void getReservationsWithTrainerDayOff() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().toString());

        List<Trainer> trainers = trainerRepository.getTrainers();

        LocalDate dayOffDate = LocalDate.now().plusDays(1);

        DayOff dayOff = DayOff.builder()
                .trainer(trainers.get(0))
                .dayOffDate(LocalDate.now().plusDays(1))
                .build();

        trainerRepository.saveDayOff(dayOff);

        Reservation reservation = Reservation.builder()
                .reservationDate(dayOffDate.atStartOfDay())
                .trainerId(dayOff.getTrainer().getTrainerId())
                .isDayOff(true)
                .build();

        reservationRepository.saveReservation(reservation);

        PersonalDetail personalDetails = personalDetailRepository.getTrainerDetail(1L)
                .orElseThrow();

        String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                personalDetails.getPersonalDetailId());

        // when
        ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.statusCode()).isEqualTo(200);
            List<ReservationDto.Response> content = result.body().jsonPath()
                    .getList("data", ReservationDto.Response.class);
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

        Member member = memberRepository.getMember(1L).orElseThrow();

        LocalDateTime reqeustDate = LocalDateTime.now().plusWeeks(2);

        Reservation reservation = Reservation.builder()
                .reservationDate(reqeustDate)
                .trainerId(1L)
                .memberId(1L)
                .name(member.getName())
                .dayOfWeek(reqeustDate.getDayOfWeek())
                .priority(0)
                .build();

        reservationRepository.saveReservation(reservation);

        // when
        ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.statusCode()).isEqualTo(200);
            List<ReservationDto.Response> content = result.body().jsonPath()
                    .getList("data", ReservationDto.Response.class);
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

        Member member = memberRepository.getMember(1L).orElseThrow();

        LocalDateTime reqeustDate = LocalDateTime.now().plusMonths(1).minusDays(1).minusSeconds(1);

        Reservation reservation = Reservation.builder()
                .reservationDate(reqeustDate)
                .trainerId(1L)
                .memberId(1L)
                .name(member.getName())
                .dayOfWeek(reqeustDate.getDayOfWeek())
                .priority(0)
                .build();

        reservationRepository.saveReservation(reservation);

        // when
        ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.statusCode()).isEqualTo(200);
            List<ReservationDto.Response> content = result.body().jsonPath()
                    .getList("data", ReservationDto.Response.class);
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

        Member member = memberRepository.getMember(1L).orElseThrow();

        LocalDateTime reqeustDate = LocalDateTime.now().plusMonths(1).minusSeconds(1);

        Reservation reservation = Reservation.builder()
                .reservationDate(reqeustDate)
                .trainerId(1L)
                .memberId(1L)
                .name(member.getName())
                .dayOfWeek(reqeustDate.getDayOfWeek())
                .priority(0)
                .build();

        reservationRepository.saveReservation(reservation);

        // when
        ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.statusCode()).isEqualTo(200);
            List<ReservationDto.Response> content = result.body().jsonPath()
                    .getList("data", ReservationDto.Response.class);
            softly.assertThat(content.size()).isEqualTo(0);
        });
    }
}
