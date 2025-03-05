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

            when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(ReservationResult.Reservations.from(response));

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

            when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(ReservationResult.Reservations.from(response));

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

            when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(ReservationResult.Reservations.from(response));

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

            when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(ReservationResult.Reservations.from(response));

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

            when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(ReservationResult.Reservations.from(response));

            //when & then
            mockMvc.perform(get("/v1/reservations")
                            .queryParam("date", requestDate)
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

            when(reservationFacade.getReservations(any(LocalDate.class), any(SecurityUser.class))).thenReturn(ReservationResult.Reservations.from(response));

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
    class GetReservationControllerTest {
        @Test
        @DisplayName("트레이너가 예약 상세 목록을 조회한다.")
        void getReservationWithTrainer() throws Exception {
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

            when(reservationFacade.getReservation(any(Long.class))).thenReturn(result);

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
        void getReservationWithNotFound() throws Exception {
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
            when(reservationFacade.getReservation(any(Long.class))).thenThrow(
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
        void getReservationWithTrainer() throws Exception {
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

            ReservationResult.ReservationWaitingMember result =
                    ReservationResult.ReservationWaitingMember.builder()
                            .reservation(Reservation.builder()
                                    .reservationDates(List.of(requestDate.plusDays(1))).build())
                            .personalDetail(PersonalDetail.builder().personalDetailId(2L).build())
                            .build();

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
        void getReservationWithNotFound() throws Exception {
            //given
            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("강산")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            ReservationResult.ReservationWaitingMember result =
                    ReservationResult.ReservationWaitingMember.builder()
                            .reservation(Reservation.builder()
                                    .reservationDates(List.of(LocalDateTime.now().plusDays(1))).build())
                            .personalDetail(PersonalDetail.builder().personalDetailId(2L).build())
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
        @DisplayName("잘못된 형태의 날짜로 설정하면 예외를 반환한다.")
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
                    .andExpect(jsonPath("$.msg").value("현재 날짜보다 이전 날짜는 설정이 불가능 합니다."))
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
                    .andExpect(jsonPath("$.msg").value("현재 날짜보다 이전 날짜는 설정이 불가능 합니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());

        }
    }

    @Nested
    @DisplayName("세션 예약 Controller TEST")
    class ReserveSessionControllerTest {

        @Test
        @DisplayName("트레이너가 세션 예약 성공")
        void reserveSessionWithTrainer() throws Exception {
            //given
            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
                    .trainerId(1L)
                    .memberId(1L)
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

            when(reservationFacade.reserveSession(any(ReservationCriteria.ReserveSession.class),
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
        void reserveSessionWithMember() throws Exception {
            //given
            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
                    .trainerId(1L)
                    .memberId(1L)
                    .name("멤버1")
                    .dates(List.of(LocalDateTime.now().plusSeconds(2)))
                    .build();

            Reservation reservation = Reservation.builder().reservationId(1L).build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(reservationFacade.reserveSession(any(ReservationCriteria.ReserveSession.class)
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
        void reserveSessionWithNoMemberId() throws Exception {
            //given

            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
            when(reservationFacade.reserveSession(any(ReservationCriteria.ReserveSession.class),
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
        void reserveSessionWithNoDate() throws Exception {
            //given

            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
            when(reservationFacade.reserveSession(any(ReservationCriteria.ReserveSession.class),
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
        void reserveSessionWithNoName() throws Exception {
            //given
            ReservationRequestDto.ReserveSession request = ReservationRequestDto.ReserveSession.builder()
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
            when(reservationFacade.reserveSession(any(ReservationCriteria.ReserveSession.class),
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