package spring.fitlinkbe.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import spring.fitlinkbe.domain.common.ConnectingInfoRepository;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationRepository;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.member.dto.MemberDto;

public class MemberControllerTest extends BaseIntegrationTest {

    @Autowired
    TestDataHandler testDataHandler;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ConnectingInfoRepository connectingInfoRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Nested
    @DisplayName("멤버 트레이너 연결 요청 성공")
    public class MemberConnectTest {
        private static final String MEMBER_CONNECT_API = "/v1/members/connect";

        @Test
        @DisplayName("멤버 트레이너 연결 요청 성공")
        public void memberConnectSuccess() throws Exception {
            // given
            // 멤버와 트레이너가 있을 때
            String trainerCode = "AB1423";
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            PersonalDetail trainerPersonalDetail = testDataHandler.getPersonalDetail(trainer.getTrainerId());
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 멤버가 트레이너와 연결 요청을 보낼 때
            MemberDto.MemberConnectRequest request = new MemberDto.MemberConnectRequest(trainerCode);
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(MEMBER_CONNECT_API, requestBody, token);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                // 연결 정보가 저장되었는지 확인
                ConnectingInfo connectingInfo = connectingInfoRepository.getConnectingInfo(member.getMemberId(), trainer.getTrainerId());
                softly.assertThat(connectingInfo).isNotNull();
                softly.assertThat(connectingInfo.getTrainer().getTrainerId()).isEqualTo(trainer.getTrainerId());
                softly.assertThat(connectingInfo.getMember().getMemberId()).isEqualTo(member.getMemberId());
                softly.assertThat(connectingInfo.getStatus()).isEqualTo(ConnectingInfo.ConnectingStatus.REQUESTED);

                // 알림 정보가 생성되었는지 확인
                Notification notification = notificationRepository.getNotification(trainerPersonalDetail.getPersonalDetailId());
                softly.assertThat(notification).isNotNull();
                softly.assertThat(notification.getRefType()).isEqualTo(Notification.NotificationType.CONNECT);
                softly.assertThat(notification.getRefId()).isEqualTo(connectingInfo.getConnectingInfoId());
                softly.assertThat(notification.getIsRead()).isFalse();
                softly.assertThat(notification.getIsSent()).isTrue();
            });
        }

        @Test
        @DisplayName("멤버 트레이너 연결 요청 실패 - 트레이너 코드가 올바르지 않을 때")
        public void memberConnectFailByInvalidTrainerCode() throws Exception {
            // given
            // 멤버와 트레이너가 있을 때
            String trainerCode = "AB1423";
            Member member = testDataHandler.createMember();
            testDataHandler.createTrainer(trainerCode);
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 멤버가 트레이너와 연결 요청을 보낼 때
            MemberDto.MemberConnectRequest request = new MemberDto.MemberConnectRequest("AB1234");
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(MEMBER_CONNECT_API, requestBody, token);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(404);
                softly.assertThat(response.data()).isNull();
            });
        }

        @Test
        @DisplayName("멤버 트레이너 연결 요청 실패 - 멤버가 이미 연결된 트레이너가 있을 때")
        public void memberConnectFailByAlreadyConnected() throws Exception {
            // given
            // 멤버와 트레이너가 있을 때
            String trainerCode = "AB1423";
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer(trainerCode);
            testDataHandler.connectMemberAndTrainer(member, trainer);
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 멤버가 트레이너와 연결 요청을 보낼 때
            MemberDto.MemberConnectRequest request = new MemberDto.MemberConnectRequest(trainerCode);
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = post(MEMBER_CONNECT_API, requestBody, token);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(409);
                softly.assertThat(response.data()).isNull();
            });
        }


    }
}
