package spring.fitlinkbe.interfaces.controller.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import spring.fitlinkbe.application.reservation.ReservationFacade;
import spring.fitlinkbe.application.reservation.criteria.ReservationCriteria;
import spring.fitlinkbe.application.reservation.criteria.ReservationResult;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationRequestDto;
import spring.fitlinkbe.support.argumentresolver.LoginMemberArgumentResolver;
import spring.fitlinkbe.support.security.AuthTokenProvider;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.RESERVATION_NOT_FOUND;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.*;
import static spring.fitlinkbe.domain.reservation.Session.Status.SESSION_COMPLETED;
import static spring.fitlinkbe.domain.reservation.Session.Status.SESSION_NOT_ATTEND;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationFacade reservationFacade;

    @MockitoBean
    private AuthTokenProvider authTokenProvider;

    @MockitoBean
    private PersonalDetailRepository personalDetailRepository;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Nested
    @DisplayName("예약 목록 조회 Controller TEST")
    class GetReservationsControllerTest {
        @Test
        @DisplayName("트레이너가 예약 목록을 조회한다.")
        void getReservationsWithTrainer() throws Exception {

            //given
            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .member(Member.builder().memberId(1L).build())
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .sessionInfo(SessionInfo.builder().SessionInfoId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
                    .dayOfWeek(LocalDateTime.now().getDayOfWeek())
                    .name("홍길동")
                    .status(RESERVATION_APPROVED)
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
                    .member(Member.builder().memberId(1L).build())
                    .sessionInfo(SessionInfo.builder().SessionInfoId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
                    .dayOfWeek(LocalDateTime.now().getDayOfWeek())
                    .name("홍길동")
                    .status(RESERVATION_WAITING)
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
                    .member(Member.builder().memberId(1L).build())
                    .sessionInfo(SessionInfo.builder().SessionInfoId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
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
                    .member(Member.builder().memberId(1L).build())
                    .sessionInfo(SessionInfo.builder().SessionInfoId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
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
                            .queryParam("date", requestDate)
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("인증을 거치지 않은 유저가 예약 목록을 조회하면 success를 false를 반환한다")
        void getReservationsWithNoAuthUser() throws Exception {

            //given
            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .member(Member.builder().memberId(1L).build())
                    .sessionInfo(SessionInfo.builder().SessionInfoId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
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
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("예약 상세 조회 Controller TEST")
    class GetReservationDetailControllerTest {
        @Test
        @DisplayName("트레이너가 예약 상세 목록을 조회한다.")
        void getReservationDetailWithTrainer() throws Exception {
            //given
            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .member(Member.builder().memberId(1L).build())
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .sessionInfo(SessionInfo.builder().SessionInfoId(1L).build())
                    .reservationDates(List.of(LocalDateTime.now()))
                    .dayOfWeek(LocalDateTime.now().getDayOfWeek())
                    .name("홍길동")
                    .build();

            Session session = Session.builder()
                    .sessionId(1L)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("강산")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            ReservationResult.ReservationDetail result = ReservationResult.ReservationDetail
                    .from(reservation, session, personalDetail);

            when(reservationFacade.getReservationDetail(any(Long.class))).thenReturn(result);

            //when & then
            mockMvc.perform(get("/v1/reservations/%s".formatted(reservation.getReservationId()))
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
        @DisplayName("없는 예약을 조회하면 success를 false를 반환한다.")
        void getReservationDetailWithNotFound() throws Exception {
            //given
            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("강산")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            //when
            when(reservationFacade.getReservationDetail(any(Long.class))).thenThrow(
                    new CustomException(RESERVATION_NOT_FOUND,
                            RESERVATION_NOT_FOUND.getMsg())
            );
            //then
            mockMvc.perform(get("/v1/reservations/%s".formatted(10L))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("예약 상세 대기 목록 조회 Controller TEST")
    class GetWaitingMembersControllerTest {
        @Test
        @DisplayName("트레이너가 예약 상세 대기 목록을 조회한다.")
        void getWaitingMembersWithTrainer() throws Exception {
            //given
            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("강산")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            Reservation result =
                    Reservation.builder()
                            .member(Member.builder().memberId(1L).build())
                            .reservationDates(List.of(requestDate.plusDays(1))).build();

            when(reservationFacade.getWaitingMembers(any(LocalDateTime.class), any(SecurityUser.class)))
                    .thenReturn(List.of(result));

            //when & then
            mockMvc.perform(get("/v1/reservations/waiting-members/%s".formatted(requestDate))
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
        @DisplayName("예약 날짜를 안넣어준 경우 success를 false를 반환한다.")
        void getWaitingMembersWithNotFound() throws Exception {
            //given
            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("강산")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);
            Reservation result =
                    Reservation.builder()
                            .reservationDates(List.of(LocalDateTime.now().plusDays(1)))
                            .build();

            when(reservationFacade.getWaitingMembers(any(LocalDateTime.class), any(SecurityUser.class)))
                    .thenReturn(List.of(result));

            //when & then
            mockMvc.perform(get("/v1/reservations/waiting-members/%s".formatted(""))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().is5xxServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("예약 불가 설정 Controller TEST")
    class SetDisabledTimeControllerTest {
        @Test
        @DisplayName("예약 불가능한 날짜를 설정한다.")
        void setDisabledDate() throws Exception {
            //given
            ReservationRequestDto.SetDisabledTime request = ReservationRequestDto.SetDisabledTime.builder()
                    .date(LocalDateTime.now().plusSeconds(1))
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("강산")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_APPROVED)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .build();

            when(reservationFacade.setDisabledReservation(any(ReservationCriteria.SetDisabledTime.class)
                    , any(SecurityUser.class)))
                    .thenReturn(reservation);

            //when & then
            mockMvc.perform(post("/v1/reservations/availability/disable")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data.reservationId").value(1L));

        }

        @Test
        @DisplayName("예약 불가로 설정하고 싶은 날짜를 넣지 않으면 예외를 반환한다.")
        void setDisabledDateWithStrangeForm() throws Exception {
            //given
            ReservationRequestDto.SetDisabledTime request = ReservationRequestDto.SetDisabledTime.builder()
                    .date(null)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("강산")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .build();

            when(reservationFacade.setDisabledReservation(request.toCriteria(), user)).thenReturn(reservation);

            //when & then
            mockMvc.perform(post("/v1/reservations/availability/disable")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("예약 날짜는 필수입니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());

        }

        @Test
        @DisplayName("현재보다 이전 날짜를 예약 불가능한 날짜로 설정하면 예외를 반환한다.")
        void setDisabledDateBeforeNow() throws Exception {
            //given
            ReservationRequestDto.SetDisabledTime request = ReservationRequestDto.SetDisabledTime.builder()
                    .date(LocalDateTime.now().minusDays(1))
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("강산")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .build();

            when(reservationFacade.setDisabledReservation(request.toCriteria(), user)).thenReturn(reservation);

            //when & then
            mockMvc.perform(post("/v1/reservations/availability/disable")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("현재 날짜보다 이전일 수 없습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());

        }
    }

    @Nested
    @DisplayName("세션 예약 Controller TEST")
    class CreateReservationControllerTest {

        @Test
        @DisplayName("트레이너가 세션 예약 성공")
        void createReservationWithTrainer() throws Exception {
            //given
            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .name("멤버1")
                    .dates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L)
                    .status(RESERVATION_APPROVED)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.createReservation(any(ReservationCriteria.Create.class),
                    any(SecurityUser.class))).thenReturn(reservation);

            //when & then
            mockMvc.perform(post("/v1/reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data.reservationId").value(1L));
        }

        @Test
        @DisplayName("멤버가 세션 예약 성공")
        void createReservationWithMember() throws Exception {
            //given
            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .name("멤버1")
                    .dates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L)
                    .status(RESERVATION_WAITING)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.createReservation(any(ReservationCriteria.Create.class)
                    , any(SecurityUser.class))).thenReturn(reservation);

            //when & then
            mockMvc.perform(post("/v1/reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data.reservationId").value(1L));
        }

        @Test
        @DisplayName("세션 예약 실패 - memberId 누락")
        void createReservationWithNoMemberId() throws Exception {
            //given

            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .name("멤버1")
                    .dates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L).build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            //when
            when(reservationFacade.createReservation(any(ReservationCriteria.Create.class),
                    any(SecurityUser.class))).thenReturn(reservation);

            //then
            mockMvc.perform(post("/v1/reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("유저 ID는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("세션 예약 실패 - date 정보 누락")
        void createReservationWithNoDate() throws Exception {
            //given

            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .name("멤버1")
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L).build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            //when
            when(reservationFacade.createReservation(any(ReservationCriteria.Create.class),
                    any(SecurityUser.class))).thenReturn(reservation);

            //then
            mockMvc.perform(post("/v1/reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("예약 요청 날짜는 비어있을 수 없습니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("세션 예약 실패 - name 정보 누락")
        void createReservationWithNoName() throws Exception {
            //given
            ReservationRequestDto.Create request = ReservationRequestDto.Create.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .dates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .build();


            Reservation reservation = Reservation.builder().reservationId(1L).build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            //when
            when(reservationFacade.createReservation(any(ReservationCriteria.Create.class),
                    any(SecurityUser.class))).thenReturn(reservation);

            //then
            mockMvc.perform(post("/v1/reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("이름은 필수값 입니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

    }

    @Nested
    @DisplayName("고정 세션 예약 Controller TEST")
    class CreateFixedReservationReservationControllerTest {

        @Test
        @DisplayName("트레이너가 고정 세션 예약 성공")
        void createFixedReservationWithTrainer() throws Exception {
            //given
            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .memberId(1L)
                    .name("멤버1")
                    .dates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L).status(RESERVATION_APPROVED)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .build();
            List<Reservation> reservations = List.of(reservation);

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.createFixedReservation(any(ReservationCriteria.CreateFixed.class),
                    any(SecurityUser.class))).thenReturn(reservations);

            //when & then
            mockMvc.perform(post("/v1/reservations/fixed-reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].reservationId").value(1L))
                    .andExpect(jsonPath("$.data[0].status").value(RESERVATION_APPROVED.getName()));
        }

        @Test
        @DisplayName("고정 세션 예약 실패 - memberID 정보 누락")
        void createFixedReservationWithNoMemberId() throws Exception {
            //given
            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .name("멤버1")
                    .dates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L).status(RESERVATION_APPROVED).build();
            List<Reservation> reservations = List.of(reservation);

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.createFixedReservation(any(ReservationCriteria.CreateFixed.class),
                    any(SecurityUser.class))).thenReturn(reservations);

            //when & then
            mockMvc.perform(post("/v1/reservations/fixed-reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("유저 ID는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("고정 세션 예약 실패 - name 정보 누락")
        void createFixedReservationWithNoName() throws Exception {
            //given
            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .memberId(1L)
                    .dates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L).status(RESERVATION_APPROVED).build();
            List<Reservation> reservations = List.of(reservation);

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.createFixedReservation(any(ReservationCriteria.CreateFixed.class),
                    any(SecurityUser.class))).thenReturn(reservations);

            //when & then
            mockMvc.perform(post("/v1/reservations/fixed-reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("이름은 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("고정 세션 예약 실패 - dates 정보 누락")
        void createFixedReservationWithNoDates() throws Exception {
            //given
            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .name("멤버1")
                    .memberId(1L)
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L).status(RESERVATION_APPROVED).build();
            List<Reservation> reservations = List.of(reservation);

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.createFixedReservation(any(ReservationCriteria.CreateFixed.class),
                    any(SecurityUser.class))).thenReturn(reservations);

            //when & then
            mockMvc.perform(post("/v1/reservations/fixed-reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("예약 요청 날짜는 비어있을 수 없습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("고정 세션 예약 실패 - 현재 날짜보다 이전 dates 정보")
        void createFixedReservationWithBeforeDates() throws Exception {
            //given
            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .name("멤버1")
                    .memberId(1L)
                    .dates(List.of(LocalDateTime.now().minusSeconds(2)))
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L).status(RESERVATION_APPROVED).build();
            List<Reservation> reservations = List.of(reservation);

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.createFixedReservation(any(ReservationCriteria.CreateFixed.class),
                    any(SecurityUser.class))).thenReturn(reservations);

            //when & then
            mockMvc.perform(post("/v1/reservations/fixed-reservations")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("현재 날짜보다 이전 날짜는 설정이 불가능 합니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

    }

    @Nested
    @DisplayName("예약 취소 Controller TEST")
    class CancelReservationControllerTest {

        @Test
        @DisplayName("트레이너가 예약 취소 성공")
        void cancelReservationWithTrainer() throws Exception {
            //given
            LocalDateTime cancelDate = LocalDateTime.now().plusHours(2);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelReason("개인 사정으로 인한 취소")
                    .cancelDate(cancelDate)
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder().reservationId(reservationId)
                    .status(RESERVATION_CANCELLED)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.cancelReservation(any(ReservationCriteria.Cancel.class),
                    any(SecurityUser.class))).thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/cancel".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data.reservationId").value(1L))
                    .andExpect(jsonPath("$.data.status").value(RESERVATION_CANCELLED.getName()));
        }

        @Test
        @DisplayName("멤버가 예약 취소 성공")
        void cancelReservationWithMember() throws Exception {
            //given
            LocalDateTime cancelDate = LocalDateTime.now().plusHours(2);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelReason("개인 사정으로 인한 취소")
                    .cancelDate(cancelDate)
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder().reservationId(reservationId)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .status(RESERVATION_CANCEL_REQUEST).build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.cancelReservation(any(ReservationCriteria.Cancel.class),
                    any(SecurityUser.class))).thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/cancel".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data").isNotEmpty())
                    .andExpect(jsonPath("$.data.reservationId").value(1L))
                    .andExpect(jsonPath("$.data.status").value(RESERVATION_CANCEL_REQUEST.getName()));
        }

        @Test
        @DisplayName("예약 취소 실패 - 취소 사유 부재")
        void cancelReservationWithNoReason() throws Exception {
            //given
            LocalDateTime cancelDate = LocalDateTime.now().plusHours(2);

            ReservationRequestDto.Cancel request = ReservationRequestDto.Cancel.builder()
                    .cancelDate(cancelDate)
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder().reservationId(reservationId).status(RESERVATION_CANCEL_REQUEST).build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.cancelReservation(any(ReservationCriteria.Cancel.class),
                    any(SecurityUser.class))).thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/cancel".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("취소 사유는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }


    @Nested
    @DisplayName("예약 승인 Controller TEST")
    class ApproveReservationControllerTest {
        @Test
        @DisplayName("트레이너의 예약 승인 - 성공")
        void approveReservation() throws Exception {
            //given
            ReservationRequestDto.Approve request = ReservationRequestDto.Approve.builder()
                    .memberId(1L)
                    .reservationDate(LocalDateTime.now().plusSeconds(2))
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_APPROVED)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.approveReservation(any(ReservationCriteria.Approve.class),
                    any(SecurityUser.class))).thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/approve".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data.status").value(RESERVATION_APPROVED.getName()));
        }


        @Test
        @DisplayName("트레이너의 예약 실패 - 멤버 ID 부재")
        void approveReservationWithNoMemberId() throws Exception {
            //given
            ReservationRequestDto.Approve request = ReservationRequestDto.Approve.builder()
                    .reservationDate(LocalDateTime.now().plusSeconds(2))
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_APPROVED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.approveReservation(any(ReservationCriteria.Approve.class),
                    any(SecurityUser.class))).thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/approve".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("유저 ID는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("진행한 PT 처리 Controller TEST")
    class CompleteSessionControllerTest {
        @Test
        @DisplayName("트레이너의 PT 처리 성공 - 참석 O")
        void completeSessionJoin() throws Exception {
            //given
            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .isJoin(true)
                    .memberId(1L)
                    .build();

            Long reservationId = 1L;

            Session result = Session.builder()
                    .sessionId(1L)
                    .status(SESSION_COMPLETED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.completeSession(any(ReservationCriteria.Complete.class),
                    any(SecurityUser.class))).thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/sessions/complete".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data.status").value(SESSION_COMPLETED.getName()));
        }

        @Test
        @DisplayName("트레이너의 PT 처리 성공 - 참석 X")
        void completeSessionNotJoin() throws Exception {
            //given
            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .isJoin(false)
                    .memberId(1L)
                    .build();

            Long reservationId = 1L;

            Session result = Session.builder()
                    .sessionId(1L)
                    .status(SESSION_NOT_ATTEND)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.completeSession(any(ReservationCriteria.Complete.class),
                    any(SecurityUser.class))).thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/sessions/complete".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data.status").value(SESSION_NOT_ATTEND.getName()));
        }

        @Test
        @DisplayName("트레이너의 PT 처리 실패 - 참석 여부 부재")
        void completeSessionWithNoJoin() throws Exception {
            //given
            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .memberId(1L)
                    .build();

            Long reservationId = 1L;

            Session result = Session.builder()
                    .sessionId(1L)
                    .status(SESSION_COMPLETED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.completeSession(any(ReservationCriteria.Complete.class),
                    any(SecurityUser.class))).thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/sessions/complete".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("참석 여부는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("트레이너의 PT 처리 실패 - 멤버 ID 부재")
        void completeSessionWithNoMemberId() throws Exception {
            //given
            ReservationRequestDto.Complete request = ReservationRequestDto.Complete.builder()
                    .isJoin(true)
                    .build();

            Long reservationId = 1L;

            Session result = Session.builder()
                    .sessionId(1L)
                    .status(SESSION_COMPLETED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.completeSession(any(ReservationCriteria.Complete.class),
                    any(SecurityUser.class))).thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/sessions/complete".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("유저 ID는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("멤버의 예약 변경 요청 Controller TEST")
    class ChangeReqeustReservationControllerTest {
        @Test
        @DisplayName("멤버의 예약 변경 요청 성공")
        void changeReqeustReservation() throws Exception {
            //given
            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(LocalDateTime.now().plusDays(1))
                    .changeRequestDate(LocalDateTime.now().plusDays(2))
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_CHANGE_REQUEST)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.changeRequestReservation(any(ReservationCriteria.ChangeReqeust.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/change-request".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data.status").value(RESERVATION_CHANGE_REQUEST.getName()));
        }

        @Test
        @DisplayName("멤버의 예약 변경 요청 실패 - 현재보다 이전 날짜로 요청")
        void changeReqeustReservationBeforeReservationDate() throws Exception {
            //given
            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(LocalDateTime.now().minusDays(1))
                    .changeRequestDate(LocalDateTime.now().plusDays(2))
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_CHANGE_REQUEST)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.changeRequestReservation(any(ReservationCriteria.ChangeReqeust.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/change-request".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("현재 날짜보다 이전일 수 없습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("멤버의 예약 변경 요청 실패 - 예약 날짜 부재")
        void changeReqeustReservationNoReservationDate() throws Exception {
            //given
            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .changeRequestDate(LocalDateTime.now().plusDays(2))
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_CHANGE_REQUEST)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.changeRequestReservation(any(ReservationCriteria.ChangeReqeust.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/change-request".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("예약 날짜는 필수입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("멤버의 예약 변경 요청 실패 - 예약 날짜와 변경 날짜 동일")
        void changeReqeustReservationSameDate() throws Exception {
            //given
            ReservationRequestDto.ChangeReqeust request = ReservationRequestDto.ChangeReqeust
                    .builder()
                    .reservationDate(LocalDateTime.now().plusDays(1))
                    .changeRequestDate(LocalDateTime.now().plusDays(1))
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_CHANGE_REQUEST)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.changeRequestReservation(any(ReservationCriteria.ChangeReqeust.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/change-request".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("예약 날짜와 변경 날짜가 같을 수 없습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }


    }

    @Nested
    @DisplayName("예약 변경 승인 Controller TEST")
    class ChangeApproveReservationControllerTest {
        @Test
        @DisplayName("멤버의 예약 변경 승인 성공")
        void changeApproveReservation() throws Exception {
            //given
            LocalDateTime approveDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.ChangeApproval request = ReservationRequestDto.ChangeApproval
                    .builder()
                    .memberId(1L)
                    .isApprove(true)
                    .approveDate(approveDate)
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_APPROVED)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.changeApproveReservation(any(ReservationCriteria.ChangeApproval.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/change-approve".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data.status").value(RESERVATION_APPROVED.getName()));
        }

        @Test
        @DisplayName("멤버의 예약 변경 승인 실패 - 멤버 ID 부재")
        void changeApproveReservationNoMemberId() throws Exception {
            //given
            LocalDateTime approveDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.ChangeApproval request = ReservationRequestDto.ChangeApproval
                    .builder()
                    .isApprove(true)
                    .approveDate(approveDate)
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_APPROVED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.changeApproveReservation(any(ReservationCriteria.ChangeApproval.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/change-approve".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("유저 ID는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("멤버의 예약 변경 승인 실패 - 승인 여부 부재")
        void changeApproveReservationNoIsApprove() throws Exception {
            LocalDateTime approveDate = LocalDateTime.now().plusHours(1);

            //given
            ReservationRequestDto.ChangeApproval request = ReservationRequestDto.ChangeApproval
                    .builder()
                    .memberId(1L)
                    .approveDate(approveDate)
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_APPROVED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.changeApproveReservation(any(ReservationCriteria.ChangeApproval.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/change-approve".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("승인 여부는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("예약 취소 승인 Controller TEST")
    class CancelApproveReservationControllerTest {
        @Test
        @DisplayName("멤버의 예약 취소 승인 성공")
        void cancelApproveReservation() throws Exception {
            //given
            ReservationRequestDto.CancelApproval request = ReservationRequestDto.CancelApproval
                    .builder()
                    .memberId(1L)
                    .isApprove(true)
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .reservationDates(List.of(LocalDateTime.now()))
                    .status(RESERVATION_CANCELLED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.cancelApproveReservation(any(ReservationCriteria.CancelApproval.class),
                    any(SecurityUser.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/cancel-approve".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data.status").value(RESERVATION_CANCELLED.getName()));
        }

        @Test
        @DisplayName("멤버의 예약 취소 승인 실패 - 멤버 ID 부재")
        void cancelApproveReservationNoMemberId() throws Exception {
            //given
            ReservationRequestDto.CancelApproval request = ReservationRequestDto.CancelApproval
                    .builder()
                    .isApprove(true)
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_CANCELLED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.cancelApproveReservation(any(ReservationCriteria.CancelApproval.class),
                    any(SecurityUser.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/cancel-approve".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("유저 ID는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("멤버의 예약 취소 승인 실패 - 승인 여부 부재")
        void cancelApproveReservationNoIsApprove() throws Exception {
            //given
            ReservationRequestDto.CancelApproval request = ReservationRequestDto.CancelApproval
                    .builder()
                    .memberId(1L)
                    .build();

            Long reservationId = 1L;

            Reservation result = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_CANCELLED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.cancelApproveReservation(any(ReservationCriteria.CancelApproval.class),
                    any(SecurityUser.class)))
                    .thenReturn(result);

            //when & then
            mockMvc.perform(post("/v1/reservations/%s/cancel-approve".formatted(reservationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("승인 여부는 필수값 입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    private String getAccessToken(PersonalDetail personalDetail) {

        String accessToken = "mockedAccessToken";
        when(authTokenProvider.createAccessToken(PersonalDetail.Status.NORMAL, personalDetail.getPersonalDetailId(),
                personalDetail.getUserRole()))
                .thenReturn(accessToken);

        when(authTokenProvider.getPersonalDetailIdFromAccessToken("mockedAccessToken"))
                .thenReturn(personalDetail.getPersonalDetailId());

        when(personalDetailRepository.getById(personalDetail.getPersonalDetailId()))
                .thenReturn(personalDetail);

        return accessToken;
    }

}