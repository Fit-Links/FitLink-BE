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
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.domain.trainer.AvailableTimeRepository;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.trainer.dto.AvailableTimesDto;
import spring.fitlinkbe.interfaces.controller.trainer.dto.TrainerInfoDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TrainerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestDataHandler testDataHandler;

    @Autowired
    AvailableTimeRepository availableTimeRepository;

    @Nested
    @DisplayName("트레이너 내 정보 조회 테스트")
    class GetTrainerInfoTest {
        private static final String URL = "/v1/trainers/me";

        @Test
        @DisplayName("트레이너 내 정보 조회 성공")
        void getTrainerInfoSuccess() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            PersonalDetail personalDetail = testDataHandler.getTrainerPersonalDetail(trainer.getTrainerId());
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 내 정보 조회 요청을 한다면
            ExtractableResponse<Response> result = get(URL, token);

            // then
            // 내 정보 조회가 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                TrainerInfoDto.Response trainerInfo = readValue(result.body().jsonPath().prettify(), TrainerInfoDto.Response.class);
                softly.assertThat(trainerInfo.trainerId()).isEqualTo(trainer.getTrainerId());
                softly.assertThat(trainerInfo.name()).isEqualTo(personalDetail.getName());
                softly.assertThat(trainerInfo.birthDate()).isEqualTo(personalDetail.getBirthDate());
                softly.assertThat(trainerInfo.phoneNumber()).isEqualTo(personalDetail.getPhoneNumber());
                softly.assertThat(trainerInfo.profilePictureUrl()).isEqualTo(personalDetail.getProfilePictureUrl());
            });
        }
    }

    @Nested
    @DisplayName("트레이너 내 정보 수정 테스트")
    class UpdateTrainerInfoTest {
        private static final String URL = "/v1/trainers/me";

        @Test
        @DisplayName("트레이너 내 정보 수정 성공")
        void updateTrainerInfoSuccess() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 내 정보 수정 요청을 한다면

            TrainerInfoDto.TrainerUpdateRequest updateRequest = new TrainerInfoDto.TrainerUpdateRequest("김민성", "01092321123");
            String requestBody = writeValueAsString(updateRequest);
            ExtractableResponse<Response> result = patch(URL, requestBody, token);

            // then
            // 내 정보 수정이 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                TrainerInfoDto.TrainerUpdateResponse trainerUpdateResponse = readValue(result.body().jsonPath().prettify(), TrainerInfoDto.TrainerUpdateResponse.class);
                softly.assertThat(trainerUpdateResponse.trainerId()).isEqualTo(trainer.getTrainerId());
                softly.assertThat(trainerUpdateResponse.name()).isEqualTo(updateRequest.name());
                softly.assertThat(trainerUpdateResponse.phoneNumber()).isEqualTo(updateRequest.phoneNumber());
            });
        }

        @Test
        @DisplayName("트레이너 내 정보 수정 성공 - 이름만 수정")
        void updateTrainerInfoSuccessWithName() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            PersonalDetail personalDetail = testDataHandler.getTrainerPersonalDetail(trainer.getTrainerId());
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 내 정보 수정 요청을 한다면
            TrainerInfoDto.TrainerUpdateRequest updateRequest = new TrainerInfoDto.TrainerUpdateRequest("김민성", null);
            String requestBody = writeValueAsString(updateRequest);
            ExtractableResponse<Response> result = patch(URL, requestBody, token);

            // then
            // 내 정보 수정이 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                TrainerInfoDto.TrainerUpdateResponse trainerUpdateResponse = readValue(result.body().jsonPath().prettify(), TrainerInfoDto.TrainerUpdateResponse.class);
                softly.assertThat(trainerUpdateResponse.trainerId()).isEqualTo(trainer.getTrainerId());
                softly.assertThat(trainerUpdateResponse.name()).isEqualTo(updateRequest.name());
                softly.assertThat(trainerUpdateResponse.phoneNumber()).isEqualTo(personalDetail.getPhoneNumber());
            });
        }

        @Test
        @DisplayName("트레이너 내 정보 수정 성공 - 전화번호만 수정")
        void updateTrainerInfoSuccessWithPhoneNumber() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            PersonalDetail personalDetail = testDataHandler.getTrainerPersonalDetail(trainer.getTrainerId());
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 내 정보 수정 요청을 한다면
            TrainerInfoDto.TrainerUpdateRequest updateRequest = new TrainerInfoDto.TrainerUpdateRequest(null, "01092321123");
            String requestBody = writeValueAsString(updateRequest);
            ExtractableResponse<Response> result = patch(URL, requestBody, token);

            // then
            // 내 정보 수정이 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                TrainerInfoDto.TrainerUpdateResponse trainerUpdateResponse = readValue(result.body().jsonPath().prettify(), TrainerInfoDto.TrainerUpdateResponse.class);
                softly.assertThat(trainerUpdateResponse.trainerId()).isEqualTo(trainer.getTrainerId());
                softly.assertThat(trainerUpdateResponse.name()).isEqualTo(personalDetail.getName());
                softly.assertThat(trainerUpdateResponse.phoneNumber()).isEqualTo(updateRequest.phoneNumber());
            });
        }

        @Test
        @DisplayName("트레이너 내 정보 수정 실패 - 이름, 전화번호 모두 null")
        void updateTrainerInfoFailWithBothNull() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 내 정보 수정 요청을 한다면
            TrainerInfoDto.TrainerUpdateRequest updateRequest = new TrainerInfoDto.TrainerUpdateRequest(null, null);
            String requestBody = writeValueAsString(updateRequest);
            ExtractableResponse<Response> result = patch(URL, requestBody, token);

            // then
            // 내 정보 수정이 실패한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.data()).isNull();
                softly.assertThat(response.msg()).isEqualTo("이름과 전화번호 중 하나는 반드시 있어야 합니다.");
            });
        }
    }

    @Nested
    @DisplayName("트레이너 코드 조회 테스트")
    class GetTrainerCodeTest {
        private static final String URL = "/v1/trainers/me/trainer-code";

        @Test
        @DisplayName("트레이너 코드 조회 성공")
        void getTrainerCodeSuccess() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 코드 조회 요청을 한다면
            ExtractableResponse<Response> result = get(URL, token);

            // then
            // 코드 조회가 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                TrainerInfoDto.TrainerCodeResponse code = readValue(result.body().jsonPath().prettify(), TrainerInfoDto.TrainerCodeResponse.class);
                softly.assertThat(code.trainerCode()).isEqualTo(trainerCode);
            });
        }
    }

    @Nested
    @DisplayName("트레이너 수업 가능 시간 조회 테스트")
    class GetTrainerAvailableTimesTest {
        private static final String URL = "/v1/trainers/me/available-times";

        @Test
        @DisplayName("트레이너 수업 가능 시간 조회 성공 - 현재 적용된 수업 시간 존재, 미래에 적용될 수업 시간 존재")
        void getTrainerAvailableTimesSuccess() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // 현재 적용된 수업 가능 시간 생성
            createAvailableTimes(trainer, LocalDate.now().minusDays(3));

            // 미래에 적용될 수업 가능 시간 생성
            LocalDate scheduledDate = LocalDate.now().plusDays(3);
            createAvailableTimes(trainer, scheduledDate);


            // when
            // 트레이너가 수업 가능 시간 조회 요청을 한다면
            ExtractableResponse<Response> result = get(URL, token);

            // then
            // 수업 가능 시간 조회가 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                AvailableTimesDto.Response availableTimes = readValue(result.body().jsonPath().prettify(), AvailableTimesDto.Response.class);

                softly.assertThat(availableTimes.currentSchedules().schedules().size()).isEqualTo(4);
                softly.assertThat(availableTimes.scheduledChanges().schedules().size()).isEqualTo(4);
                softly.assertThat(availableTimes.scheduledChanges().applyAt()).isEqualTo(scheduledDate);
            });
        }

        @Test
        @DisplayName("트레이너 수업 가능 시간 조회 성공 - 현재 적용된 수업 시간 존재, 미래에 적용될 수업 시간 없음")
        void getTrainerAvailableTimesSuccessWithoutScheduledChanges() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // 현재 적용된 수업 가능 시간 생성
            createAvailableTimes(trainer, LocalDate.now().minusDays(3));

            // when
            // 트레이너가 수업 가능 시간 조회 요청을 한다면
            ExtractableResponse<Response> result = get(URL, token);

            // then
            // 수업 가능 시간 조회가 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                AvailableTimesDto.Response availableTimes = readValue(result.body().jsonPath().prettify(), AvailableTimesDto.Response.class);

                softly.assertThat(availableTimes.currentSchedules().schedules().size()).isEqualTo(4);
                softly.assertThat(availableTimes.scheduledChanges()).isNull();
            });
        }

        @Test
        @DisplayName("트레이너 수업 가능 시간 조회 성공 - 현재 적용된 수업 시간 없음, 미래에 적용될 수업 시간 존재")
        void getTrainerAvailableTimesSuccessWithoutCurrentSchedules() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // 미래에 적용될 수업 가능 시간 생성
            LocalDate scheduledDate = LocalDate.now().plusDays(3);
            createAvailableTimes(trainer, scheduledDate);

            // when
            // 트레이너가 수업 가능 시간 조회 요청을 한다면
            ExtractableResponse<Response> result = get(URL, token);

            // then
            // 수업 가능 시간 조회가 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                AvailableTimesDto.Response availableTimes = readValue(result.body().jsonPath().prettify(), AvailableTimesDto.Response.class);

                softly.assertThat(availableTimes.currentSchedules()).isNull();
                softly.assertThat(availableTimes.scheduledChanges().schedules().size()).isEqualTo(4);
                softly.assertThat(availableTimes.scheduledChanges().applyAt()).isEqualTo(scheduledDate);
            });
        }

        private void createAvailableTimes(Trainer trainer, LocalDate date) {
            // 수업 가능 시간 생성
            testDataHandler.createAvailableTime(trainer, DayOfWeek.MONDAY, date);
            testDataHandler.createAvailableTime(trainer, DayOfWeek.TUESDAY, date);
            testDataHandler.createAvailableTime(trainer, DayOfWeek.WEDNESDAY, date);
            testDataHandler.createAvailableTime(trainer, DayOfWeek.THURSDAY, date);
        }
    }

    @Nested
    @DisplayName("트레이너 수업 가능 시간 추가 테스트")
    class AddTrainerAvailableTimesTest {
        private static final String URL = "/v1/trainers/me/available-times";

        @Test
        @DisplayName("트레이너 수업 가능 시간 추가 성공 - 현재 적용된 날짜만 존재할 때 미래 날짜 추가")
        void addTrainerAvailableTimesSuccess() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 수업 가능 시간 추가 요청을 한다면
            LocalDate now = LocalDate.now();
            AvailableTimesDto.AddRequest addRequest = getAvailableTimeAddRequest(now);
            String requestBody = writeValueAsString(addRequest);
            ExtractableResponse<Response> result = post(URL, requestBody, token);

            // then
            // 수업 가능 시간 추가가 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(201);
                softly.assertThat(response.data()).isNull();

                List<AvailableTime> availableTimes = availableTimeRepository.getAvailableTimes(trainer.getTrainerId(), now);
                softly.assertThat(availableTimes.size()).isEqualTo(4);
                availableTimes.forEach(availableTime -> {
                    softly.assertThat(availableTime.getTrainer().getTrainerId()).isEqualTo(trainer.getTrainerId());
                    softly.assertThat(availableTime.getApplyAt()).isEqualTo(now);
                });
            });
        }

        @Test
        @DisplayName("트레이너 수업 가능 시간 추가 성공 - 미래에 적용될 날짜만 있을 때 현재 날짜 추가")
        void addTrainerAvailableTimesSuccessWithScheduledDate() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // 미래에 적용될 수업 가능 시간 생성
            LocalDate scheduledDate = LocalDate.now().plusDays(3);
            testDataHandler.createAvailableTime(trainer, DayOfWeek.MONDAY, scheduledDate);

            // when
            // 트레이너가 수업 가능 시간 추가 요청을 한다면
            LocalDate now = LocalDate.now();
            AvailableTimesDto.AddRequest addRequest = getAvailableTimeAddRequest(now);
            String requestBody = writeValueAsString(addRequest);
            ExtractableResponse<Response> result = post(URL, requestBody, token);

            // then
            // 수업 가능 시간 추가가 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(201);
                softly.assertThat(response.data()).isNull();

                List<AvailableTime> availableTimes = availableTimeRepository.getAvailableTimes(trainer.getTrainerId(), now);
                softly.assertThat(availableTimes.size()).isEqualTo(4);
                availableTimes.forEach(availableTime -> {
                    softly.assertThat(availableTime.getTrainer().getTrainerId()).isEqualTo(trainer.getTrainerId());
                    softly.assertThat(availableTime.getApplyAt()).isEqualTo(now);
                });
            });
        }


        @Test
        @DisplayName("트레이너 수업 가능 시간 추가 실패 - 이미 적용된 수업 + 적용 대기중인 스케줄 존재")
        void addTrainerAvailableTimesFailWithAlreadyApplied() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // 현재 적용된 수업 가능 시간 생성
            testDataHandler.createAvailableTime(trainer, DayOfWeek.MONDAY, LocalDate.now());

            // 미래에 적용될 수업 가능 시간 생성
            LocalDate scheduledDate = LocalDate.now().plusDays(3);
            testDataHandler.createAvailableTime(trainer, DayOfWeek.MONDAY, scheduledDate);

            // when
            // 트레이너가 수업 가능 시간 추가 요청을 한다면
            LocalDate now = LocalDate.now().plusDays(4);
            AvailableTimesDto.AddRequest addRequest = getAvailableTimeAddRequest(now);
            String requestBody = writeValueAsString(addRequest);
            ExtractableResponse<Response> result = post(URL, requestBody, token);

            // then
            // 수업 가능 시간 추가가 실패한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(409);
                softly.assertThat(response.data()).isNull();
                softly.assertThat(response.msg()).isEqualTo("이미 적용 대기중인 스케줄이 있습니다.");
            });
        }

        @Test
        @DisplayName("트레이너 수업 가능 시간 추가 실패 - 오늘 날짜 적용시 이미 적용된 수업 시간이 있을 때")
        void addTrainerAvailableTimesFailWithAlreadyAppliedToday() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // 현재 적용된 수업 가능 시간 생성
            testDataHandler.createAvailableTime(trainer, DayOfWeek.MONDAY, LocalDate.now());

            // when
            // 트레이너가 수업 가능 시간 추가 요청을 한다면
            LocalDate now = LocalDate.now();
            AvailableTimesDto.AddRequest addRequest = getAvailableTimeAddRequest(now);
            String requestBody = writeValueAsString(addRequest);
            ExtractableResponse<Response> result = post(URL, requestBody, token);

            // then
            // 수업 가능 시간 추가가 실패한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(409);
                softly.assertThat(response.data()).isNull();
                softly.assertThat(response.msg()).isEqualTo("이미 적용된 수업 시간이 있습니다.");
            });
        }

        @Test
        @DisplayName("트레이너 수업 가능 시간 추가 실패 - 적용 날짜가 오늘보다 전 날짜일 경우")
        void addTrainerAvailableTimesFailWithPastDate() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 수업 가능 시간 추가 요청을 한다면
            LocalDate pastDate = LocalDate.now().minusDays(1);
            AvailableTimesDto.AddRequest addRequest = getAvailableTimeAddRequest(pastDate);
            String requestBody = writeValueAsString(addRequest);
            ExtractableResponse<Response> result = post(URL, requestBody, token);

            // then
            // 수업 가능 시간 추가가 실패한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.data()).isNull();
                softly.assertThat(response.msg()).isEqualTo("적용 날짜는 오늘 이후여야 합니다.");
            });
        }


        @ParameterizedTest
        @MethodSource("invalidRequests")
        @DisplayName("트레이너 수업 가능 시간 추가 실패 - 잘못된 요청 파라미터")
        void addTrainerAvailableTimesFailWithInvalidRequest(AvailableTimesDto.AddRequest addRequest) throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 수업 가능 시간 추가 요청을 한다면
            String requestBody = writeValueAsString(addRequest);
            ExtractableResponse<Response> result = post(URL, requestBody, token);

            // then
            // 수업 가능 시간 추가가 실패한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.data()).isNull();
            });
        }


        private static Stream<AvailableTimesDto.AddRequest> invalidRequests() {
            // 요일이 겹치는 경우
            List<AvailableTimesDto.AvailableTimeRequest> availableTimes1 = List.of(
                    new AvailableTimesDto.AvailableTimeRequest(DayOfWeek.MONDAY, false, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                    new AvailableTimesDto.AvailableTimeRequest(DayOfWeek.MONDAY, false, LocalTime.of(9, 0), LocalTime.of(12, 0))
            );

            // 시작 시간이 끝 시간보다 늦은 경우
            List<AvailableTimesDto.AvailableTimeRequest> availableTimes2 = List.of(
                    new AvailableTimesDto.AvailableTimeRequest(DayOfWeek.MONDAY, false, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                    new AvailableTimesDto.AvailableTimeRequest(DayOfWeek.TUESDAY, false, LocalTime.of(9, 0), LocalTime.of(8, 0))
            );

            List<AvailableTimesDto.AvailableTimeRequest> availableTimes3 = List.of(
                    new AvailableTimesDto.AvailableTimeRequest(DayOfWeek.MONDAY, false, LocalTime.of(9, 0), LocalTime.of(12, 0))
            );

            return Stream.of(
                    new AvailableTimesDto.AddRequest(null, null),
                    new AvailableTimesDto.AddRequest(LocalDate.now(), null),
                    new AvailableTimesDto.AddRequest(null, availableTimes3),
                    new AvailableTimesDto.AddRequest(LocalDate.now(), List.of()),
                    new AvailableTimesDto.AddRequest(LocalDate.now().minusDays(1), List.of()),
                    new AvailableTimesDto.AddRequest(LocalDate.now(), availableTimes1),
                    new AvailableTimesDto.AddRequest(LocalDate.now(), availableTimes2)
            );
        }


        private AvailableTimesDto.AddRequest getAvailableTimeAddRequest(LocalDate applyAt) {
            List<AvailableTimesDto.AvailableTimeRequest> availableTimes = List.of(
                    new AvailableTimesDto.AvailableTimeRequest(DayOfWeek.MONDAY, false, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                    new AvailableTimesDto.AvailableTimeRequest(DayOfWeek.TUESDAY, false, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                    new AvailableTimesDto.AvailableTimeRequest(DayOfWeek.WEDNESDAY, false, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                    new AvailableTimesDto.AvailableTimeRequest(DayOfWeek.THURSDAY, false, LocalTime.of(9, 0), LocalTime.of(12, 0))
            );

            return new AvailableTimesDto.AddRequest(applyAt, availableTimes);
        }
    }


    @Nested
    @DisplayName("트레이너 수업 가능 시간 삭제 테스트")
    class DeleteTrainerAvailableTimesTest {
        private static final String URL = "/v1/trainers/me/available-times";

        @Test
        @DisplayName("트레이너 수업 가능 시간 삭제 성공")
        void deleteTrainerAvailableTimesSuccess() throws Exception {
            // given
            // 트레이너 정보가 있을 때
            String trainerCode = "AB1423";
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            LocalDate now = LocalDate.now();
            testDataHandler.createAvailableTime(trainer, DayOfWeek.MONDAY, now);
            testDataHandler.createAvailableTime(trainer, DayOfWeek.TUESDAY, now);
            testDataHandler.createAvailableTime(trainer, DayOfWeek.WEDNESDAY, now);
            testDataHandler.createAvailableTime(trainer, DayOfWeek.THURSDAY, now);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 수업 가능 시간 삭제 요청을 한다면
            Map<String, String> parameter = Map.of("applyAt", now.toString());
            ExtractableResponse<Response> result = delete(URL, parameter, token);

            // then
            // 수업 가능 시간 삭제가 성공한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(204);
                softly.assertThat(response.data()).isNull();

                List<AvailableTime> availableTimes = availableTimeRepository.getAvailableTimes(trainer.getTrainerId(), now);
                softly.assertThat(availableTimes.size()).isEqualTo(0);
            });
        }
    }
}
