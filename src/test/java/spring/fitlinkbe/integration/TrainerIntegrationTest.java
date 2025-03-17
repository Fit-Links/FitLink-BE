package spring.fitlinkbe.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.trainer.dto.TrainerInfoDto;

public class TrainerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestDataHandler testDataHandler;

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
            ExtractableResponse<Response> result = patch(URL, token, requestBody);

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
            ExtractableResponse<Response> result = patch(URL, token, requestBody);

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
            ExtractableResponse<Response> result = patch(URL, token, requestBody);

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
            ExtractableResponse<Response> result = patch(URL, token, requestBody);

            // then
            // 내 정보 수정이 실패한다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(400);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
            });
        }
    }

}
