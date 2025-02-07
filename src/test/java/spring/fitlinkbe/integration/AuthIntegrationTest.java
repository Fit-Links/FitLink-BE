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
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.infra.common.personaldetail.PersonalDetailEntity;
import spring.fitlinkbe.infra.common.personaldetail.PersonalDetailJpaRepository;
import spring.fitlinkbe.infra.common.token.TokenEntity;
import spring.fitlinkbe.infra.common.token.TokenJpaRepository;
import spring.fitlinkbe.infra.member.MemberEntity;
import spring.fitlinkbe.infra.member.MemberJpaRepository;
import spring.fitlinkbe.infra.member.WorkoutScheduleEntity;
import spring.fitlinkbe.infra.member.WorkoutScheduleJpaRepository;
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
    PersonalDetailJpaRepository personalDetailJpaRepository;

    @Autowired
    WorkoutScheduleJpaRepository workoutScheduleJpaRepository;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    TokenJpaRepository tokenJpaRepository;


    @Nested
    @DisplayName("Member Register API Integration Test")
    class MemberRegisterTest {
        private static final String MEMBER_REGISTER_API = "/v1/auth/members/register";

        @Test
        @DisplayName("멤버 등록 성공")
        public void registerMemberSuccess() throws Exception {
            // given
            // REQUIRED_SMS 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_SMS);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId());

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

                PersonalDetailEntity updatedPersonalDetail = personalDetailJpaRepository.findById(personalDetail.getPersonalDetailId()).orElseThrow();
                softly.assertThat(updatedPersonalDetail.getStatus()).isEqualTo(PersonalDetail.Status.NORMAL);
                softly.assertThat(updatedPersonalDetail.getName()).isEqualTo(request.name());
                softly.assertThat(updatedPersonalDetail.getBirthDate()).isEqualTo(request.birthDate());
                softly.assertThat(updatedPersonalDetail.getPhoneNumber()).isEqualTo(request.phoneNumber());
                softly.assertThat(updatedPersonalDetail.getProfilePictureUrl()).isEqualTo(request.profileUrl());
                softly.assertThat(updatedPersonalDetail.getGender()).isEqualTo(request.gender());

                MemberEntity member = memberJpaRepository.findById(updatedPersonalDetail.getMember().getMemberId()).orElseThrow();
                softly.assertThat(member).isNotNull();
                softly.assertThat(member.getName()).isEqualTo(request.name());
                softly.assertThat(member.getBirthDate()).isEqualTo(request.birthDate());
                softly.assertThat(member.getPhoneNumber()).isEqualTo(request.phoneNumber());

                List<WorkoutScheduleEntity> workoutSchedules = workoutScheduleJpaRepository.findAllByMember_MemberId(member.getMemberId());
                workoutSchedules.sort(Comparator.comparing(WorkoutScheduleEntity::getDayOfWeek));

                List<AuthDto.WorkoutScheduleRequest> workoutScheduleRequests = new ArrayList<>(request.workoutSchedule());
                workoutScheduleRequests.sort(Comparator.comparing(AuthDto.WorkoutScheduleRequest::dayOfWeek));

                softly.assertThat(workoutSchedules).hasSize(workoutScheduleRequests.size());

                for (int i = 0; i < workoutSchedules.size(); i++) {
                    WorkoutScheduleEntity workoutSchedule = workoutSchedules.get(i);
                    AuthDto.WorkoutScheduleRequest workoutScheduleRequest = workoutScheduleRequests.get(i);

                    softly.assertThat(workoutSchedule.getDayOfWeek()).isEqualTo(workoutScheduleRequest.dayOfWeek());
                    softly.assertThat(workoutSchedule.getPreferenceTimes()).hasSize(workoutScheduleRequest.preferenceTimes().size());

                    for (int j = 0; j < workoutSchedule.getPreferenceTimes().size(); j++) {
                        softly.assertThat(workoutSchedule.getPreferenceTimes().get(j)).isEqualTo(workoutScheduleRequest.preferenceTimes().get(j));
                    }
                }

                TokenEntity token = tokenJpaRepository.findByPersonalDetail_PersonalDetailId(personalDetail.getPersonalDetailId()).orElseThrow();
                softly.assertThat(response.data().refreshToken()).isEqualTo(token.getRefreshToken());
            });
        }

        @Test
        @DisplayName("멤버 등록 실패 - 멤버 상태 REQUIRED_SMS 가 아닌 경우")
        public void registerMemberFailBecauseOfNotRequiredSmsStatus() throws Exception {
            // given
            // NORMAL 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.NORMAL);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId());

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
            // REQUIRED_SMS 상태의 유저가 있을 때

            PersonalDetail personalDetail = testDataHandler.createPersonalDetail(PersonalDetail.Status.REQUIRED_SMS);
            String accessToken = authTokenProvider.createAccessToken(personalDetail.getStatus(), personalDetail.getPersonalDetailId());

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
                            "01012345678",
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            null,
                            "01012345678",
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            null,
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            "01012345678",
                            null,
                            "http://test.com",
                            null
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            "01012345678",
                            PersonalDetail.Gender.MALE,
                            "http://test.com",
                            List.of(workoutScheduleRequest1)
                    ),
                    new AuthDto.MemberRegisterRequest(
                            "홍길동",
                            LocalDate.of(1990, 1, 1),
                            "01012345678",
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
                    "01012345678",
                    PersonalDetail.Gender.MALE,
                    "http://test.com",
                    workoutSchedules
            );
        }

    }
}
