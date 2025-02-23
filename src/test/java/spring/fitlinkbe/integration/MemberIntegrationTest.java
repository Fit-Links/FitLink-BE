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
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationRepository;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.member.dto.MemberDto;
import spring.fitlinkbe.interfaces.controller.member.dto.MemberInfoDto;

public class MemberIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestDataHandler testDataHandler;

    @Autowired
    ConnectingInfoRepository connectingInfoRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Nested
    @DisplayName("멤버 트레이너 연결 요청 테스트")
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

    @Nested
    @DisplayName("멤버 트레이너 연결 해제 요청 테스트")
    public class MemberDisconnectTest {
        private static final String MEMBER_DISCONNECT_API = "/v1/members/disconnect";

        @Test
        @DisplayName("멤버 트레이너 연결 해제 요청 성공")
        public void memberDisconnectSuccess() throws Exception {
            // given
            // 멤버와 트레이너가 연결되어 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 멤버가 트레이너와 연결 해제 요청을 보낸다면

            ExtractableResponse<Response> result = post(MEMBER_DISCONNECT_API, token);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });
                softly.assertThat(response).isNotNull();

                // 연결 정보가 삭제되었는지 확인
                ConnectingInfo deletedConnectingInfo = connectingInfoRepository.getConnectingInfo(member.getMemberId(), trainer.getTrainerId());
                softly.assertThat(deletedConnectingInfo).isNotNull();
                softly.assertThat(deletedConnectingInfo.getStatus()).isEqualTo(ConnectingInfo.ConnectingStatus.DISCONNECTED);


                // 알림 정보가 생성되었는지 확인
                PersonalDetail trainerPersonalDetail = testDataHandler.getPersonalDetail(trainer.getTrainerId());
                Notification notification = notificationRepository.getNotification(trainerPersonalDetail.getPersonalDetailId(),
                        Notification.NotificationType.DISCONNECT);
                softly.assertThat(notification).isNotNull();
            });
        }

        @Test
        @DisplayName("멤버 트레이너 연결 해제 요청 실패 - 이미 연결 요청 중일 때")
        public void memberDisconnectFailByRequested() throws Exception {
            // given
            // 멤버와 트레이너가 연결 요청 중일 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.requestConnectTrainer(member, trainer);
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 멤버가 트레이너와 연결 해제 요청을 보낸다면
            ExtractableResponse<Response> result = post(MEMBER_DISCONNECT_API, token);

            // then
            // 연결 요청 중이라는 응답을 받는다
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

        @Test
        @DisplayName("멤버 트레이너 연결 해제 요청 실패 - 연결 정보가 없을 때")
        public void memberDisconnectFailByNotConnected() throws Exception {
            // given
            // 트레이너와 연동된 회원이 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 트레이너와 연결 해제 요청을 보낸다면
            ExtractableResponse<Response> result = post(MEMBER_DISCONNECT_API, token);

            // then
            // 연결 정보가 없다는 응답을 받는다
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
    }

    @Nested
    @DisplayName("멤버 내 정보 조회 테스트")
    public class MemberInfoTest {
        private static final String MEMBER_INFO_API = "/v1/members/me";

        @Test
        @DisplayName("멤버 내 정보 조회 성공")
        public void memberInfoSuccess() throws Exception {
            // given
            // NORMAL 상태의 멤버, 트레이너, 세션 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            SessionInfo sessionInfo = testDataHandler.createSessionInfo(member, trainer);
            String token = testDataHandler.createTokenFromMember(member);
            testDataHandler.connectMemberAndTrainer(member, trainer);

            // when
            // 멤버가 자신의 정보를 조회한다면
            ExtractableResponse<Response> result = get(MEMBER_INFO_API, token);

            // then
            // 자신의 정보를 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<MemberInfoDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                MemberInfoDto.Response data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);
                softly.assertThat(data.memberId()).isEqualTo(member.getMemberId());
                softly.assertThat(data.name()).isEqualTo(member.getName());
                softly.assertThat(data.profilePictureUrl()).isEqualTo(member.getProfilePictureUrl());
                softly.assertThat(data.trainerName()).isEqualTo(trainer.getName());
                softly.assertThat(data.trainerId()).isEqualTo(trainer.getTrainerId());
                softly.assertThat(data.sessionInfo().sessionInfoId()).isEqualTo(sessionInfo.getSessionInfoId());
                softly.assertThat(data.sessionInfo().remainingCount()).isEqualTo(sessionInfo.getRemainingCount());
                softly.assertThat(data.sessionInfo().totalCount()).isEqualTo(sessionInfo.getTotalCount());
            });
        }
    }

    @Nested
    @DisplayName("회원 정보 수정 (PATCH) API 테스트")
    public class MemberUpdateTest {
        private static final String MEMBER_UPDATE_API = "/v1/members/me";

        @Test
        @DisplayName("회원 정보 수정 성공")
        public void memberUpdateSuccess() throws Exception {
            // given
            // 회원이 있을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 정보를 수정할 때
            String newName = "newName";
            String newPhoneNumber = "01092831232";
            MemberInfoDto.MemberUpdateRequest request = new MemberInfoDto.MemberUpdateRequest(newName, newPhoneNumber);
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = patch(MEMBER_UPDATE_API, requestBody, token);

            // then
            // 수정된 정보를 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<MemberInfoDto.MemberUpdateResponse> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                MemberInfoDto.MemberUpdateResponse data = response.data();
                softly.assertThat(data.memberId()).isEqualTo(member.getMemberId());
                softly.assertThat(data.name()).isEqualTo(newName);
                softly.assertThat(data.phoneNumber()).isEqualTo(newPhoneNumber);
            });
        }

        @Test
        @DisplayName("회원 정보 수정 실패 - 이름, 전화번호 둘 다 입력하지 않았을 때")
        public void memberUpdateFailByEmptyNameAndPhoneNumber() throws Exception {
            // given
            // 회원이 있을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 정보를 수정할 때 이름, 전화번호 둘 다 입력하지 않았을 때
            MemberInfoDto.MemberUpdateRequest request = new MemberInfoDto.MemberUpdateRequest(null, null);
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = patch(MEMBER_UPDATE_API, requestBody, token);

            // then
            // 이름, 전화번호 중 하나는 반드시 입력해야 한다는 응답을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<MemberInfoDto.MemberUpdateResponse> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.data()).isNull();
            });
        }
    }
}
