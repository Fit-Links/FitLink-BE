package spring.fitlinkbe.interfaces.controller.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import spring.fitlinkbe.application.reservation.ReservationFacade;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.support.argumentresolver.LoginMemberArgumentResolver;
import spring.fitlinkbe.support.security.AuthTokenProvider;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationFacade reservationFacade;

    @MockitoBean
    private AuthTokenProvider authTokenProvider;

    @MockitoBean
    private PersonalDetailRepository personalDetailRepository;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Test
    @DisplayName("트레이너가 예약 목록을 조회한다.")
    void getReservationsWithTrainer() throws Exception {

        //given
        Reservation reservation = Reservation.builder()
                .reservationId(1L)
                .memberId(1L)
                .sessionInfoId(1L)
                .reservationDate(LocalDateTime.now())
                .priority(1)
                .dayOfWeek(LocalDateTime.now().getDayOfWeek())
                .name("홍길동")
                .build();

        List<Reservation> response = List.of(reservation);  // 실제 예약 추가

        LocalDate requestDate = LocalDate.parse("2024-04-20");

        PersonalDetail personalDetail = PersonalDetail.builder()
                .personalDetailId(1L)
                .name("강산")
                .memberId(null)
                .trainerId(1L)
                .build();

        SecurityUser user = new SecurityUser(personalDetail);

        String accessToken = getAccessToken(personalDetail);

        when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(response);

        //when & then
        mockMvc.perform(get("/v1/reservations")
                        .queryParam("date", requestDate.toString())
                        .header("Authorization", "Bearer " + accessToken)
                        .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.msg").value("OK"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @DisplayName("멤버가 예약 목록을 조회한다.")
    void getReservationsWithMember() throws Exception {

        //given
        Reservation reservation = Reservation.builder()
                .reservationId(1L)
                .memberId(1L)
                .sessionInfoId(1L)
                .reservationDate(LocalDateTime.now())
                .priority(1)
                .dayOfWeek(LocalDateTime.now().getDayOfWeek())
                .name("홍길동")
                .build();

        List<Reservation> response = List.of(reservation);  // 실제 예약 추가

        LocalDate requestDate = LocalDate.parse("2024-04-20");

        PersonalDetail personalDetail = PersonalDetail.builder()
                .personalDetailId(1L)
                .name("홍길동")
                .memberId(1L)
                .trainerId(null)
                .build();

        SecurityUser user = new SecurityUser(personalDetail);
        String accessToken = getAccessToken(personalDetail);

        when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(response);

        //when & then
        mockMvc.perform(get("/v1/reservations")
                        .queryParam("date", requestDate.toString())
                        .header("Authorization", "Bearer " + accessToken)
                        .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.msg").value("OK"))
                .andExpect(jsonPath("$.data").isNotEmpty());

    }

    @Test
    @DisplayName("예약 목록이 없으면 빈 배열을 반환한다.")
    void getReservationsWithEmpty() throws Exception {

        //given
        List<Reservation> response = List.of();  // 실제 예약 추가

        LocalDate requestDate = LocalDate.parse("2024-04-20");

        PersonalDetail personalDetail = PersonalDetail.builder()
                .personalDetailId(1L)
                .name("홍길동")
                .memberId(1L)
                .trainerId(null)
                .build();

        SecurityUser user = new SecurityUser(personalDetail);
        String accessToken = getAccessToken(personalDetail);

        when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(response);

        //when //then
        mockMvc.perform(get("/v1/reservations")
                        .queryParam("date", requestDate.toString())
                        .header("Authorization", "Bearer " + accessToken)
                        .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.msg").value("OK"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("확인하고자 하는 날짜를 넣지 않으면, success를 false를 반환한다")
    void getReservationsWithEmptyDate() throws Exception {

        //given
        Reservation reservation = Reservation.builder()
                .reservationId(1L)
                .memberId(1L)
                .sessionInfoId(1L)
                .reservationDate(LocalDateTime.now())
                .priority(1)
                .dayOfWeek(LocalDateTime.now().getDayOfWeek())
                .name("홍길동")
                .build();

        List<Reservation> response = List.of(reservation);  // 실제 예약 추가

        PersonalDetail personalDetail = PersonalDetail.builder()
                .personalDetailId(1L)
                .name("강산")
                .memberId(null)
                .trainerId(1L)
                .build();

        SecurityUser user = new SecurityUser(personalDetail);

        String accessToken = getAccessToken(personalDetail);

        when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(response);

        //when & then
        mockMvc.perform(get("/v1/reservations")
                        .header("Authorization", "Bearer " + accessToken)
                        .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("정확한 날짜 형식을 넣지 않으면, success를 false를 반환한다")
    void getReservationsWithStrangeDate() throws Exception {

        //given
        Reservation reservation = Reservation.builder()
                .reservationId(1L)
                .memberId(1L)
                .sessionInfoId(1L)
                .reservationDate(LocalDateTime.now())
                .priority(1)
                .dayOfWeek(LocalDateTime.now().getDayOfWeek())
                .name("홍길동")
                .build();

        List<Reservation> response = List.of(reservation);

        String requestDate = "2024년4월20일";

        PersonalDetail personalDetail = PersonalDetail.builder()
                .personalDetailId(1L)
                .name("강산")
                .memberId(null)
                .trainerId(1L)
                .build();

        SecurityUser user = new SecurityUser(personalDetail);

        String accessToken = getAccessToken(personalDetail);

        when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(response);

        //when & then
        mockMvc.perform(get("/v1/reservations")
                        .queryParam("date", requestDate.toString())
                        .header("Authorization", "Bearer " + accessToken)
                        .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("인증을 거치지 않은 유저가 예약 목록을 조회하면 success를 false를 반환한다")
    void getReservationsWithNoAuthUser() throws Exception {

        //given
        Reservation reservation = Reservation.builder()
                .reservationId(1L)
                .memberId(1L)
                .sessionInfoId(1L)
                .reservationDate(LocalDateTime.now())
                .priority(1)
                .dayOfWeek(LocalDateTime.now().getDayOfWeek())
                .name("홍길동")
                .build();

        List<Reservation> response = List.of(reservation);  // 실제 예약 추가

        LocalDate requestDate = LocalDate.parse("2024-04-20");

        PersonalDetail personalDetail = PersonalDetail.builder()
                .personalDetailId(1L)
                .name("강산")
                .memberId(null)
                .trainerId(1L)
                .build();

        SecurityUser user = new SecurityUser(personalDetail);

        when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(response);

        //when & then
        mockMvc.perform(get("/v1/reservations")
                        .queryParam("date", requestDate.toString())
                        .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private String getAccessToken(PersonalDetail personalDetail) {

        String accessToken = "mockedAccessToken";
        when(authTokenProvider.createAccessToken(PersonalDetail.Status.NORMAL, personalDetail.getPersonalDetailId()))
                .thenReturn(accessToken);

        when(authTokenProvider.getPersonalDetailIdFromAccessToken("mockedAccessToken"))
                .thenReturn(personalDetail.getPersonalDetailId());

        when(personalDetailRepository.getById(personalDetail.getPersonalDetailId()))
                .thenReturn(personalDetail);

        return accessToken;
    }
}