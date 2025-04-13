package spring.fitlinkbe.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.member.WorkoutSchedule;
import spring.fitlinkbe.domain.member.WorkoutScheduleRepository;
import spring.fitlinkbe.domain.notification.FcmService;
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.auth.dto.AuthDto;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.support.security.AuthTokenProvider;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestDataHandler testDataHandler;

    @Autowired
    AuthTokenProvider authTokenProvider;

    @Autowired
    PersonalDetailRepository personalDetailRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TrainerRepository trainerRepository;

    @Autowired
    FcmService fcmService;

    @Autowired
    WorkoutScheduleRepository workoutScheduleRepository;

    @Autowired
    TokenRepository tokenRepository;


    @Nested
    @DisplayName("Member Register API Integration Test")
    class MemberRegisterTest {
        private static final String MEMBER_REGISTER_API = "/v1/auth/members/register";

        @Test
        @DisplayName("멤버 등록 성공")
        public void registerMemberSuccess() throws Exception {
            // given
            // REQUIRED_REGISTER 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_REGISTER);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                    personalDetail.getUserRole());

            // when
            // 멤버 등록 요청을 보낸다면

            AuthDto.MemberRegisterRequest request = getRequest();
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(MEMBER_REGISTER_API, requestBody, accessToken);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.data().accessToken()).isNotNull();
                softly.assertThat(response.data().accessToken()).isNotNull();

                PersonalDetail updatedPersonalDetail = personalDetailRepository.getById(personalDetail.getPersonalDetailId());
                softly.assertThat(updatedPersonalDetail.getStatus()).isEqualTo(PersonalDetail.Status.NORMAL);
                softly.assertThat(updatedPersonalDetail.getName()).isEqualTo(request.name());
                softly.assertThat(updatedPersonalDetail.getBirthDate()).isEqualTo(request.birthDate());
                softly.assertThat(updatedPersonalDetail.getProfilePictureUrl()).isEqualTo(request.profileUrl());
                softly.assertThat(updatedPersonalDetail.getGender()).isEqualTo(request.gender());

                Member member = memberRepository.getMember(updatedPersonalDetail.getMemberId()).orElseThrow();
                softly.assertThat(member).isNotNull();
                softly.assertThat(member.getName()).isEqualTo(request.name());
                softly.assertThat(member.getBirthDate()).isEqualTo(request.birthDate());
                softly.assertThat(member.getProfilePictureUrl()).isEqualTo(request.profileUrl());

                List<WorkoutSchedule> workoutSchedules = new ArrayList<>(workoutScheduleRepository.findAllByMemberId(member.getMemberId()));
                workoutSchedules.sort(Comparator.comparing(WorkoutSchedule::getDayOfWeek));

                List<AuthDto.WorkoutScheduleRequest> workoutScheduleRequests = new ArrayList<>(request.workoutSchedule());
                workoutScheduleRequests.sort(Comparator.comparing(AuthDto.WorkoutScheduleRequest::dayOfWeek));

                softly.assertThat(workoutSchedules).hasSize(workoutScheduleRequests.size());

                for (int i = 0; i < workoutSchedules.size(); i++) {
                    WorkoutSchedule workoutSchedule = workoutSchedules.get(i);
                    AuthDto.WorkoutScheduleRequest workoutScheduleRequest = workoutScheduleRequests.get(i);

                    softly.assertThat(workoutSchedule.getDayOfWeek()).isEqualTo(workoutScheduleRequest.dayOfWeek());
                    softly.assertThat(workoutSchedule.getPreferenceTimes()).hasSize(workoutScheduleRequest.preferenceTimes().size());

                    for (int j = 0; j < workoutSchedule.getPreferenceTimes().size(); j++) {
                        softly.assertThat(workoutSchedule.getPreferenceTimes().get(j)).isEqualTo(workoutScheduleRequest.preferenceTimes().get(j));
                    }
                }

                Token token = tokenRepository.getByPersonalDetailId(personalDetail.getPersonalDetailId());
                softly.assertThat(response.data().refreshToken()).isEqualTo(token.getRefreshToken());
            });
        }

        @Test
        @DisplayName("멤버 등록 실패 - 운동 희망일의 요일이 중복되는 경우")
        public void registerMemberFailBecauseOfDuplicateDayOfWeek() throws Exception {
            // given
            // REQUIRED_REGISTER 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_REGISTER);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                    personalDetail.getUserRole());

            // when
            // 운동 희망일의 요일이 중복되는 요청이 온다면
            AuthDto.MemberRegisterRequest request = getDuplicateDayOfWeekRequest();
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(MEMBER_REGISTER_API, requestBody, accessToken);

            // then
            // 에러를 반환한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.success()).isFalse();
            });
        }

        @Test
        @DisplayName("멤버 등록 실패 - 멤버 상태 REQUIRED_REGISTER 가 아닌 경우")
        public void registerMemberFailBecauseOfNotRequiredSmsStatus() throws Exception {
            // given
            // NORMAL 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.NORMAL);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                    personalDetail.getUserRole());

            // when
            // 멤버 등록 요청을 보낸다면
            AuthDto.MemberRegisterRequest request = getRequest();
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(MEMBER_REGISTER_API, requestBody, accessToken);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.status()).isEqualTo(403);
                softly.assertThat(response.success()).isFalse();
            });
        }

        @ParameterizedTest
        @MethodSource("invalidRequests")
        @DisplayName("멤버 등록 실패 - 필수값 누락")
        public void registerMemberFailBecauseOfMissingRequiredValue(AuthDto.MemberRegisterRequest request) throws Exception {
            // given
            // REQUIRED_REGISTER 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_REGISTER);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                    personalDetail.getUserRole());

            // when
            // 필수값이 누락된 멤버 등록 요청을 보낸다면
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(MEMBER_REGISTER_API, requestBody, accessToken);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.success()).isFalse();
            });
        }

        private AuthDto.MemberRegisterRequest getDuplicateDayOfWeekRequest() {
            List<AuthDto.WorkoutScheduleRequest> workoutScheduleRequests = List.of(
                    new AuthDto.WorkoutScheduleRequest(
                            DayOfWeek.MONDAY,
                            List.of(
                                    LocalTime.of(10, 0)
                            )
                    ),
                    new AuthDto.WorkoutScheduleRequest(
                            DayOfWeek.MONDAY,
                            List.of(
                                    LocalTime.of(12, 0)
                            )
                    )
            );

            return new AuthDto.MemberRegisterRequest(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    PersonalDetail.Gender.MALE,
                    "http://test.com",
                    workoutScheduleRequests
            );
        }

        private static Stream<AuthDto.MemberRegisterRequest> invalidRequests() {
            AuthDto.WorkoutScheduleRequest workoutScheduleRequest1 = new AuthDto.WorkoutScheduleRequest(
                    null,
                    List.of(
                            LocalTime.of(10, 0)
                    )
            );
            AuthDto.WorkoutScheduleRequest workoutScheduleRequest2 = new AuthDto.WorkoutScheduleRequest(
                    DayOfWeek.MONDAY,
                    null
            );
            return Stream.of(
                    new AuthDto.MemberRegisterRequest(
                            null,
                            LocalDate.of(1990, 1, 1),
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            null,
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            null,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            List.of(workoutScheduleRequest1)
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            List.of(workoutScheduleRequest2)
                    )
            );
        }

        private AuthDto.MemberRegisterRequest getRequest() {
            List<AuthDto.WorkoutScheduleRequest> workoutSchedules = List.of(
                    new AuthDto.WorkoutScheduleRequest(
                            DayOfWeek.MONDAY,
                            List.of(
                                    LocalTime.of(10, 0),
                                    LocalTime.of(12, 0)
                            )
                    ),
                    new AuthDto.WorkoutScheduleRequest(
                            DayOfWeek.TUESDAY,
                            List.of(
                                    LocalTime.of(10, 0),
                                    LocalTime.of(12, 0),
                                    LocalTime.of(14, 0),
                                    LocalTime.of(16, 0),
                                    LocalTime.of(18, 0)
                            )
                    )
            );

            return new AuthDto.MemberRegisterRequest(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    PersonalDetail.Gender.MALE,
                    "http://test.com",
                    workoutSchedules
            );
        }

    }

    @Nested
    @DisplayName("Trainer Register API Integration Test")
    class TrainerRegisterTest {
        private static final String TRAINER_REGISTER_API = "/v1/auth/trainers/register";

        @Test
        @DisplayName("트레이너 등록 성공")
        public void registerTrainerSuccess() throws Exception {
            // given
            // REQUIRED_REGISTER 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_REGISTER);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId()
                    , personalDetail.getUserRole());

            // when
            // 트레이너 등록 요청을 보낸다면
            AuthDto.TrainerRegisterRequest request = getTrainerRequest();
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(TRAINER_REGISTER_API, requestBody, accessToken);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.data().accessToken()).isNotNull();
                softly.assertThat(response.data().accessToken()).isNotNull();

                PersonalDetail updatedPersonalDetail = personalDetailRepository.getById(personalDetail.getPersonalDetailId());
                softly.assertThat(updatedPersonalDetail.getStatus()).isEqualTo(PersonalDetail.Status.NORMAL);
                softly.assertThat(updatedPersonalDetail.getName()).isEqualTo(request.name());
                softly.assertThat(updatedPersonalDetail.getBirthDate()).isEqualTo(request.birthDate());
                softly.assertThat(updatedPersonalDetail.getProfilePictureUrl()).isEqualTo(request.profileUrl());
                softly.assertThat(updatedPersonalDetail.getGender()).isEqualTo(request.gender());

                Trainer trainer = trainerRepository.getTrainerInfo(updatedPersonalDetail.getTrainerId()).orElseThrow();
                softly.assertThat(trainer).isNotNull();
                softly.assertThat(trainer.getTrainerCode()).isNotNull();
                softly.assertThat(trainer.getName()).isEqualTo(request.name());

                List<AvailableTime> availableTimes = trainerRepository.getTrainerAvailableTimes(trainer.getTrainerId());
                softly.assertThat(availableTimes).hasSize(request.availableTimes().size());

                Token token = tokenRepository.getByPersonalDetailId(personalDetail.getPersonalDetailId());
                softly.assertThat(response.data().refreshToken()).isEqualTo(token.getRefreshToken());
            });

        }

        @Test
        @DisplayName("트레이너 등록 성공 - 수업 가능 시간이 holiday 인 경우 시작 시간과 종료 시간은 null 가능")
        public void registerTrainerSuccessWithHoliday() throws Exception {
            // given
            // REQUIRED_REGISTER 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_REGISTER);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                    personalDetail.getUserRole());

            // when
            // 수업 가능 시간이 holiday, 시작 시간과 종료 시간이 null 인 트레이너 등록 요청을 보낸다면
            AuthDto.TrainerRegisterRequest request = getTrainerRequestWithHoliday();
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(TRAINER_REGISTER_API, requestBody, accessToken);

            // then
            // 요청에 성공해야 한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.data().accessToken()).isNotNull();
                softly.assertThat(response.data().accessToken()).isNotNull();
            });
        }

        @Test
        @DisplayName("트레이너 등록 실패 - 유저의 상태가 REQUIRED_REGISTER 가 아닌 경우")
        public void registerMemberFailBecauseOfNotRequiredSmsStatus() throws Exception {
            // given
            // NORMAL 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.NORMAL);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                    personalDetail.getUserRole());

            // when
            // 트레이너 등록 요청을 보낸다면
            AuthDto.TrainerRegisterRequest request = getTrainerRequest();
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(TRAINER_REGISTER_API, requestBody, accessToken);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.status()).isEqualTo(403);
                softly.assertThat(response.success()).isFalse();
            });
        }

        @Test
        @DisplayName("트레이너 등록 실패 - 수업 가능 시작 시간이 종료 시간보다 늦은 경우")
        public void registerTrainerFailBecauseOfInvalidTime() throws Exception {
            // given
            // REQUIRED_REGISTER 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_REGISTER);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                    personalDetail.getUserRole());

            // when
            // 수업 가능 시작 시간이 종료 시간보다 늦은 요청이 온다면
            AuthDto.TrainerRegisterRequest request = getTimeInvalidRequest();
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(TRAINER_REGISTER_API, requestBody, accessToken);

            // then
            // 에러를 반환한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.success()).isFalse();
            });
        }

        @Test
        @DisplayName("트레이너 등록 실패 - 수업 가능 시간 요일이 중복되는 경우")
        public void registerTrainerFailBecauseOfDuplicateDayOfWeek() throws Exception {
            // given
            // REQUIRED_REGISTER 상태의 유저가 있을 때
            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_REGISTER);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                    personalDetail.getUserRole());

            // when
            // 수업 가능 시간의 요일이 중복되는 요청이 온다면
            AuthDto.TrainerRegisterRequest request = getDuplicateAvailableTimeRequest();
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(TRAINER_REGISTER_API, requestBody, accessToken);

            // then
            // 에러를 반환한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.success()).isFalse();
            });
        }


        private AuthDto.TrainerRegisterRequest getTimeInvalidRequest() {
            List<AuthDto.AvailableTimeRequest> availableTimes = List.of(
                    new AuthDto.AvailableTimeRequest(
                            DayOfWeek.MONDAY,
                            true,
                            LocalTime.of(12, 0),
                            LocalTime.of(10, 0)
                    )
            );

            return new AuthDto.TrainerRegisterRequest(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    PersonalDetail.Gender.MALE,
                    "http://test.com",
                    availableTimes
            );
        }

        private AuthDto.TrainerRegisterRequest getTrainerRequestWithHoliday() {
            List<AuthDto.AvailableTimeRequest> availableTimes = List.of(
                    new AuthDto.AvailableTimeRequest(
                            DayOfWeek.MONDAY,
                            true,
                            null,
                            null
                    )
            );

            return new AuthDto.TrainerRegisterRequest(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    PersonalDetail.Gender.MALE,
                    "http://test.com",
                    availableTimes
            );
        }

        private AuthDto.TrainerRegisterRequest getDuplicateAvailableTimeRequest() {
            List<AuthDto.AvailableTimeRequest> availableTimes = List.of(
                    new AuthDto.AvailableTimeRequest(
                            DayOfWeek.MONDAY,
                            false,
                            LocalTime.of(10, 0),
                            LocalTime.of(12, 0)
                    ),
                    new AuthDto.AvailableTimeRequest(
                            DayOfWeek.MONDAY,
                            false,
                            LocalTime.of(10, 0),
                            LocalTime.of(12, 0)
                    )
            );

            return new AuthDto.TrainerRegisterRequest(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    PersonalDetail.Gender.MALE,
                    "http://test.com",
                    availableTimes
            );
        }

        @ParameterizedTest
        @MethodSource("invalidRequests")
        @DisplayName("트레이너 등록 실패 - 필수값 누락")
        public void registerMemberFailBecauseOfMissingRequiredValue(AuthDto.TrainerRegisterRequest request) throws Exception {
            // given
            // REQUIRED_REGISTER 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_REGISTER);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId(),
                    personalDetail.getUserRole());

            // when
            // 필수값이 누락된 멤버 등록 요청을 보낸다면
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(TRAINER_REGISTER_API, requestBody, accessToken);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.success()).isFalse();
            });
        }

        private static Stream<AuthDto.TrainerRegisterRequest> invalidRequests() {
            List<AuthDto.AvailableTimeRequest> availableTimeRequest1 = List.of(
                    new AuthDto.AvailableTimeRequest(
                            null,
                            false,
                            LocalTime.of(10, 0),
                            LocalTime.of(12, 0)
                    ),
                    new AuthDto.AvailableTimeRequest(
                            DayOfWeek.MONDAY,
                            null,
                            LocalTime.of(10, 0),
                            LocalTime.of(12, 0)
                    )
            );

            List<AuthDto.AvailableTimeRequest> availableTimeRequest2 = List.of(
                    new AuthDto.AvailableTimeRequest(
                            DayOfWeek.MONDAY,
                            false,
                            null,
                            LocalTime.of(12, 0)
                    ),
                    new AuthDto.AvailableTimeRequest(
                            DayOfWeek.TUESDAY,
                            false,
                            LocalTime.of(10, 0),
                            null
                    )
            );

            return Stream.of(
                    new AuthDto.TrainerRegisterRequest(
                            null,
                            LocalDate.of(1990, 1, 1),
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.TrainerRegisterRequest(
                            "홍길동",
                            null,
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.TrainerRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.TrainerRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            null,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.TrainerRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            availableTimeRequest1
                    ),
                    new AuthDto.TrainerRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            availableTimeRequest2
                    )
            );
        }


        private AuthDto.TrainerRegisterRequest getTrainerRequest() {
            List<AuthDto.AvailableTimeRequest> availableTimes = List.of(
                    new AuthDto.AvailableTimeRequest(
                            DayOfWeek.MONDAY,
                            false,
                            LocalTime.of(10, 0),
                            LocalTime.of(12, 0)
                    ),
                    new AuthDto.AvailableTimeRequest(
                            DayOfWeek.TUESDAY,
                            false,
                            LocalTime.of(10, 0),
                            LocalTime.of(12, 0)
                    )
            );

            return new AuthDto.TrainerRegisterRequest(
                    "홍길동",
                    LocalDate.of(1990, 1, 1),
                    PersonalDetail.Gender.MALE,
                    "http://test.com",
                    availableTimes
            );
        }

    }


    @Nested
    @DisplayName("회원 이메일 인증 API 테스트")
    public class EmailAuthTest {
        private static final String EMAIL_AUTH_API = "/v1/auth/email-verification-token";

        @Test
        @DisplayName("회원 이메일 인증 코드 발급 성공")
        public void sendEmailAuthCodeTest() throws Exception {
            // given
            // REQUIRED_SMS 상태의 유저가 있을 때
            Member member = testDataHandler.createMember(PersonalDetail.Status.REQUIRED_SMS);
            String accessToken = testDataHandler.createTokenFromMember(member);

            // when
            // 이메일 인증 코드 발급 요청을 보낸다면
            ExtractableResponse<Response> result = get(EMAIL_AUTH_API, accessToken);

            // then
            // 요청에 성공해야 한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.EmailAuthTokenResponse> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.data().verificationToken()).isNotNull();
            });
        }

        @Test
        @DisplayName("회원 이메일 인증 코드 발급 실패 - 회원의 상태가 REQUIRED_SMS 가 아닌 경우")
        public void sendEmailAuthCodeFailBecauseOfNotRequiredSmsStatus() throws Exception {
            // given
            // NORMAL 상태의 유저가 있을 때
            Member member = testDataHandler.createMember(PersonalDetail.Status.NORMAL);
            String accessToken = testDataHandler.createTokenFromMember(member);

            // when
            // 이메일 인증 코드 발급 요청을 보낸다면
            ExtractableResponse<Response> result = get(EMAIL_AUTH_API, accessToken);

            // then
            // 에러를 반환한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<AuthDto.EmailAuthTokenResponse> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.status()).isEqualTo(403);
                softly.assertThat(response.success()).isFalse();
            });
        }
    }
}
