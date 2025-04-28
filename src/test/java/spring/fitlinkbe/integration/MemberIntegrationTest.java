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
import spring.fitlinkbe.domain.common.ConnectingInfoRepository;
import spring.fitlinkbe.domain.common.SessionInfoRepository;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.WorkoutSchedule;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationRepository;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.common.dto.CustomPageResponse;
import spring.fitlinkbe.interfaces.controller.member.dto.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

public class MemberIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestDataHandler testDataHandler;

    @Autowired
    ConnectingInfoRepository connectingInfoRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    SessionInfoRepository sessionInfoRepository;

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
            PersonalDetail trainerPersonalDetail = testDataHandler.getTrainerPersonalDetail(trainer.getTrainerId());
            String token = testDataHandler.createTokenFromMember(member);
            testDataHandler.createTokenInfo(member);
            testDataHandler.createTokenInfo(trainer);

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
                softly.assertThat(notification.getRefType()).isEqualTo(Notification.ReferenceType.CONNECT);
                softly.assertThat(notification.getRefId()).isEqualTo(connectingInfo.getConnectingInfoId());
                softly.assertThat(notification.isSent()).isFalse();
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
            testDataHandler.createTokenInfo(member);
            testDataHandler.createTokenInfo(trainer);
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 멤버가 트레이너와 연결 해제 요청을 보낸다면

            ExtractableResponse<Response> result = post(MEMBER_DISCONNECT_API, token);

            // then
            // 연결 정보가 정상적으로 삭제된다
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
                PersonalDetail trainerPersonalDetail = testDataHandler.getTrainerPersonalDetail(trainer.getTrainerId());
                Notification notification = notificationRepository.getNotification(trainerPersonalDetail.getPersonalDetailId(),
                        Notification.NotificationType.DISCONNECT);
                softly.assertThat(notification).isNotNull();
            });
        }

        @Test
        @DisplayName("멤버 트레이너 연결 해제 요청 성공 - 연결 요청 중일 때")
        public void memberDisconnectFailByRequested() throws Exception {
            // given
            // 멤버와 트레이너가 연결 요청 중일 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.requestConnectTrainer(member, trainer);
            testDataHandler.createTokenInfo(member);
            testDataHandler.createTokenInfo(trainer);

            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 멤버가 트레이너와 연결 해제 요청을 보낸다면
            ExtractableResponse<Response> result = post(MEMBER_DISCONNECT_API, token);

            // then
            // 연결 정보가 정상적으로 삭제된다
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
                PersonalDetail trainerPersonalDetail = testDataHandler.getTrainerPersonalDetail(trainer.getTrainerId());
                Notification notification = notificationRepository.getNotification(trainerPersonalDetail.getPersonalDetailId(),
                        Notification.NotificationType.DISCONNECT);
                softly.assertThat(notification).isNotNull();
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
            // NORMAL 상태의 멤버, 트레이너, 세션 정보, PT 희망 시간 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            SessionInfo sessionInfo = testDataHandler.createSessionInfo(member, trainer);
            String token = testDataHandler.createTokenFromMember(member);
            testDataHandler.connectMemberAndTrainer(member, trainer);
            List<WorkoutSchedule> workoutSchedules = testDataHandler.createWorkoutSchedules(member);
            testDataHandler.createFixedReservation(member, trainer);
            testDataHandler.createFixedReservation(member, trainer);

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

                softly.assertThat(data.fixedReservations().size()).isEqualTo(2);

                softly.assertThat(data.workoutSchedules().size()).isEqualTo(workoutSchedules.size());
                for (WorkoutScheduleDto.Response responseDto : data.workoutSchedules()) {
                    WorkoutSchedule workoutSchedule = workoutSchedules.stream().filter(ws -> ws.getWorkoutScheduleId()
                            .equals(responseDto.workoutScheduleId())).findFirst().orElseThrow();

                    softly.assertThat(responseDto.workoutScheduleId()).isEqualTo(workoutSchedule.getWorkoutScheduleId());
                    softly.assertThat(responseDto.dayOfWeek()).isEqualTo(workoutSchedule.getDayOfWeek());
                    softly.assertThat(responseDto.preferenceTimes())
                            .containsExactlyInAnyOrderElementsOf(workoutSchedule.getPreferenceTimes());
                }
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

                PersonalDetail personalDetail = testDataHandler.getMemberPersonalDetail(member.getMemberId());
                softly.assertThat(personalDetail.getName()).isEqualTo(newName);
                softly.assertThat(personalDetail.getPhoneNumber()).isEqualTo(newPhoneNumber);
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

    @Nested
    @DisplayName("회원 정보 상세 조회 테스트")
    public class MemberDetailTest {
        private static final String MEMBER_DETAIL_API = "/v1/members/me/detail";

        @Test
        @DisplayName("회원 정보 상세 조회 성공")
        public void memberDetailSuccess() throws Exception {
            // given
            // 회원이 있을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 정보를 조회할 때
            ExtractableResponse<Response> result = get(MEMBER_DETAIL_API, token);

            // then
            // 회원의 상세 정보를 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<MemberInfoDto.DetailResponse> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                MemberInfoDto.DetailResponse data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.memberId()).isEqualTo(member.getMemberId());
                softly.assertThat(data.profilePictureUrl()).isEqualTo(member.getProfilePictureUrl());
                softly.assertThat(data.name()).isEqualTo(member.getName());
                softly.assertThat(data.birthDate()).isEqualTo(member.getBirthDate());
                softly.assertThat(data.phoneNumber()).isEqualTo(member.getPhoneNumber());
            });
        }
    }

    @Nested
    @DisplayName("회원 PT 희망 시간 수정 API 테스트")
    public class MemberPtTimeUpdateTest {
        private static final String MEMBER_PT_TIME_UPDATE_API = "/v1/members/me/workout-schedule";

        @Test
        @DisplayName("회원 PT 희망 시간 수정 성공 - PT 희망 시간이 없을 때")
        public void memberPtTimeUpdateSuccess() throws Exception {
            // given
            // 회원이 있고 PT 희망 시간이 없을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 PT 희망 시간을 수정할 때
            List<WorkoutScheduleDto.Request> request = List.of(
                    new WorkoutScheduleDto.Request(null, DayOfWeek.MONDAY,
                            List.of(LocalTime.of(12, 0), LocalTime.of(13, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.TUESDAY,
                            List.of(LocalTime.of(14, 0), LocalTime.of(15, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.WEDNESDAY,
                            List.of(LocalTime.of(16, 0), LocalTime.of(17, 0)))
            );
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = put(MEMBER_PT_TIME_UPDATE_API, requestBody, token);

            // then
            // 수정된 PT 희망 시간을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<List<WorkoutScheduleDto.Response>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                List<WorkoutScheduleDto.Response> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.size()).isEqualTo(request.size());
                for (WorkoutScheduleDto.Response responseDto : data) {
                    WorkoutScheduleDto.Request requestDto = request.stream().filter(r -> r.dayOfWeek()
                            .equals(responseDto.dayOfWeek())).findFirst().orElseThrow();

                    softly.assertThat(responseDto.dayOfWeek()).isEqualTo(requestDto.dayOfWeek());
                    softly.assertThat(responseDto.preferenceTimes()).containsExactlyInAnyOrderElementsOf(requestDto.preferenceTimes());
                }
            });
        }

        @Test
        @DisplayName("회원 PT 희망 시간 수정 성공 - PT 희망 시간이 있을 때 PT 희망 시간 수정, 삭제")
        public void memberPtTimeUpdateSuccessByExist() throws Exception {
            // given
            // 회원이 있고 PT 희망 시간이 있을 때
            Member member = testDataHandler.createMember();
            List<WorkoutSchedule> workoutSchedules = testDataHandler.createWorkoutSchedules(member);
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 PT 희망 시간을 수정할 때 기존 PT 희망 시간보다 적은 PT 희망 시간이 입력된다면
            List<WorkoutScheduleDto.Request> request = List.of(
                    new WorkoutScheduleDto.Request(workoutSchedules.get(0).getWorkoutScheduleId(), DayOfWeek.MONDAY,
                            List.of(LocalTime.of(12, 0), LocalTime.of(13, 0))),
                    new WorkoutScheduleDto.Request(workoutSchedules.get(1).getWorkoutScheduleId(), DayOfWeek.TUESDAY,
                            List.of(LocalTime.of(14, 0), LocalTime.of(15, 0))),
                    new WorkoutScheduleDto.Request(workoutSchedules.get(2).getWorkoutScheduleId(), DayOfWeek.WEDNESDAY,
                            List.of(LocalTime.of(16, 0), LocalTime.of(17, 0)))
            );
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = put(MEMBER_PT_TIME_UPDATE_API, requestBody, token);

            // then
            // 기존 데이터는 수정, 입력에서 빠진 데이터는 삭제된다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<List<WorkoutScheduleDto.Response>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                List<WorkoutScheduleDto.Response> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.size()).isEqualTo(request.size());
                for (WorkoutScheduleDto.Response responseDto : data) {
                    WorkoutScheduleDto.Request requestDto = request.stream().filter(r -> r.dayOfWeek()
                            .equals(responseDto.dayOfWeek())).findFirst().orElseThrow();

                    softly.assertThat(responseDto.dayOfWeek()).isEqualTo(requestDto.dayOfWeek());
                    softly.assertThat(responseDto.preferenceTimes()).containsExactlyInAnyOrderElementsOf(requestDto.preferenceTimes());
                }
            });
        }

        @Test
        @DisplayName("회원 PT 희망 시간 수정 성공 - PT 희망 시간이 있을 때 추가, 수정")
        public void memberPtTimeUpdateSuccessByExist2() throws Exception {
            // given
            // 회원이 있고 PT 희망 시간이 있을 때
            Member member = testDataHandler.createMember();
            List<WorkoutSchedule> workoutSchedules = testDataHandler.createWorkoutSchedules(member);
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 PT 희망 시간을 수정할 때 기존 PT 희망 시간보다 많은 PT 희망 시간이 입력된다면
            List<WorkoutScheduleDto.Request> request = List.of(
                    new WorkoutScheduleDto.Request(workoutSchedules.get(0).getWorkoutScheduleId(), DayOfWeek.MONDAY,
                            List.of(LocalTime.of(12, 0), LocalTime.of(13, 0))),
                    new WorkoutScheduleDto.Request(workoutSchedules.get(1).getWorkoutScheduleId(), DayOfWeek.TUESDAY,
                            List.of(LocalTime.of(14, 0), LocalTime.of(15, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.WEDNESDAY,
                            List.of(LocalTime.of(16, 0), LocalTime.of(17, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.THURSDAY,
                            List.of(LocalTime.of(18, 0), LocalTime.of(19, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.FRIDAY,
                            List.of(LocalTime.of(20, 0), LocalTime.of(21, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.SATURDAY,
                            List.of(LocalTime.of(22, 0), LocalTime.of(23, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.SUNDAY,
                            List.of(LocalTime.of(0, 0), LocalTime.of(1, 0)))
            );

            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = put(MEMBER_PT_TIME_UPDATE_API, requestBody, token);

            // then
            // 기존 데이터는 수정, 추가된 데이터는 추가된다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<List<WorkoutScheduleDto.Response>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                List<WorkoutScheduleDto.Response> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.size()).isEqualTo(request.size());
                for (WorkoutScheduleDto.Response responseDto : data) {
                    WorkoutScheduleDto.Request requestDto = request.stream().filter(r -> r.dayOfWeek()
                            .equals(responseDto.dayOfWeek())).findFirst().orElseThrow();

                    softly.assertThat(responseDto.dayOfWeek()).isEqualTo(requestDto.dayOfWeek());
                    softly.assertThat(responseDto.preferenceTimes()).containsExactlyInAnyOrderElementsOf(requestDto.preferenceTimes());
                }
            });
        }

        @Test
        @DisplayName("회원 PT 희망 시간 수정 실패 - PT 희망 시간이 없을 때 요일이 중복되는 경우")
        public void memberPtTimeUpdateFailByDuplicateDayOfWeek() throws Exception {
            // given
            // 회원이 있고 PT 희망 시간이 없을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 PT 희망 시간을 수정할 때 요일이 중복되는 경우
            List<WorkoutScheduleDto.Request> request = List.of(
                    new WorkoutScheduleDto.Request(null, DayOfWeek.MONDAY,
                            List.of(LocalTime.of(12, 0), LocalTime.of(13, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.MONDAY,
                            List.of(LocalTime.of(14, 0), LocalTime.of(15, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.WEDNESDAY,
                            List.of(LocalTime.of(16, 0), LocalTime.of(17, 0)))
            );
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = put(MEMBER_PT_TIME_UPDATE_API, requestBody, token);

            // then
            // 요일이 중복된다는 에러 응답을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<List<WorkoutScheduleDto.Response>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.data()).isNull();
            });
        }

        @ParameterizedTest
        @MethodSource("invalidRequests")
        @DisplayName("회원 PT 희망 시간 수정 실패 - 요일, 시간이 올바르지 않은 경우")
        public void memberPtTimeUpdateFailByInvalidRequest(List<WorkoutScheduleDto.Request> request) throws Exception {
            // given
            // 회원이 있을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 PT 희망 시간을 수정할 때 요일, 시간이 올바르지 않은 경우
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = put(MEMBER_PT_TIME_UPDATE_API, requestBody, token);

            // then
            // 요일, 시간이 올바르지 않다는 에러 응답을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<List<WorkoutScheduleDto.Response>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.data()).isNull();
            });
        }

        public static Stream<List<WorkoutScheduleDto.Request>> invalidRequests() {
            // 요일이 null 인 경우
            List<WorkoutScheduleDto.Request> invalidRequest1 = List.of(
                    new WorkoutScheduleDto.Request(null, DayOfWeek.MONDAY,
                            List.of(LocalTime.of(12, 0), LocalTime.of(13, 0))),
                    new WorkoutScheduleDto.Request(null, null,
                            List.of(LocalTime.of(14, 0), LocalTime.of(15, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.WEDNESDAY,
                            List.of(LocalTime.of(16, 0), LocalTime.of(17, 0)))
            );

            // 요일이 겹치는 경우
            List<WorkoutScheduleDto.Request> invalidRequest2 = List.of(
                    new WorkoutScheduleDto.Request(null, DayOfWeek.MONDAY,
                            List.of(LocalTime.of(12, 0), LocalTime.of(13, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.MONDAY,
                            List.of(LocalTime.of(14, 0), LocalTime.of(15, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.WEDNESDAY,
                            List.of(LocalTime.of(16, 0), LocalTime.of(17, 0)))
            );

            // 선호 시간이 null 인 경우
            List<WorkoutScheduleDto.Request> invalidRequest3 = List.of(
                    new WorkoutScheduleDto.Request(null, DayOfWeek.MONDAY,
                            List.of(LocalTime.of(12, 0), LocalTime.of(13, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.TUESDAY,
                            List.of(LocalTime.of(14, 0), LocalTime.of(15, 0))),
                    new WorkoutScheduleDto.Request(null, DayOfWeek.WEDNESDAY,
                            null)
            );

            return Stream.of(invalidRequest1, invalidRequest2, invalidRequest3);
        }
    }

    @Nested
    @DisplayName("나의 PT 내역 조회 API 테스트")
    public class MemberSessionTest {
        private static final String MEMBER_SESSION_API = "/v1/members/me/sessions";

        @Test
        @DisplayName("회원 PT 내역 조회 성공 - 전체 조회")
        public void memberSessionSuccess() throws Exception {
            // given
            // 회원, 트레이너, 세션 정보가 있을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);

            // session 7개 생성
            createSessions(member, trainer);

            // when
            // 회원이 PT 내역을 조회할 때
            int page = 0;
            int size = 10;
            String url = MEMBER_SESSION_API + "?page=" + page + "&size=" + size;
            ExtractableResponse<Response> result = get(url, token);

            // then
            // PT 내역을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberSessionDto.SessionResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(7);
                softly.assertThat(data.getTotalPages()).isEqualTo(1);
                softly.assertThat(data.getContent().size()).isEqualTo(7);
            });
        }

        @Test
        @DisplayName("회원 PT 내역 조회 성공 - 상태별 조회")
        public void memberSessionSuccessByStatus() throws Exception {
            // given
            // 회원, 트레이너, 세션 정보가 있을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);

            // session 7개 생성
            createSessions(member, trainer);

            // when
            // 회원이 PT 내역을 조회할 때 상태별로 조회할 때
            int page = 0;
            int size = 10;
            String url = MEMBER_SESSION_API + "?page=" + page + "&size=" + size + "&status=SESSION_WAITING";
            ExtractableResponse<Response> result = get(url, token);

            // then
            // PT 내역을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberSessionDto.SessionResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(3);
                softly.assertThat(data.getTotalPages()).isEqualTo(1);
                softly.assertThat(data.getContent().size()).isEqualTo(3);
            });
        }

        @Test
        @DisplayName("회원 PT 내역 조회 성공 - 페이징 조회")
        public void memberSessionSuccessByPaging() throws Exception {
            // given
            // 회원, 트레이너, 세션 정보가 있을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);

            // session 7개 생성
            createSessions(member, trainer);

            // when
            // 회원이 PT 내역을 조회할 때 페이징 조회할 때
            int page = 0;
            int size = 3;
            String url = MEMBER_SESSION_API + "?page=" + page + "&size=" + size;
            ExtractableResponse<Response> result = get(url, token);

            // then
            // PT 내역을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberSessionDto.SessionResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(7);
                softly.assertThat(data.getTotalPages()).isEqualTo(3);
                softly.assertThat(data.getContent().size()).isEqualTo(3);
            });
        }

        @Test
        @DisplayName("회원 PT 내역 조회 성공 - 페이징 기본값 (page=0, size=5) 조회")
        public void memberSessionSuccessByDefaultPaging() throws Exception {
            // given
            // 회원, 트레이너, 세션 정보가 있을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);

            // session 7개 생성
            createSessions(member, trainer);

            // when
            // 회원이 PT 내역을 조회할 때 페이징 기본값으로 조회할 때
            ExtractableResponse<Response> result = get(MEMBER_SESSION_API, token);

            // then
            // PT 내역을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberSessionDto.SessionResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(7);
                softly.assertThat(data.getTotalPages()).isEqualTo(2);
                softly.assertThat(data.getContent().size()).isEqualTo(5);
            });
        }

        @Test
        @DisplayName("회원 PT 내역 조회 성공 - 세션 정보 없을 때 빈리스트")
        public void memberSessionSuccessByEmpty() throws Exception {
            // given
            // 회원이 있을 때
            Member member = testDataHandler.createMember();
            String token = testDataHandler.createTokenFromMember(member);

            // when
            // 회원이 PT 내역을 조회할 때 세션 정보가 없을 때
            int page = 0;
            int size = 10;
            String url = MEMBER_SESSION_API + "?page=" + page + "&size=" + size;
            ExtractableResponse<Response> result = get(url, token);

            // then
            // 빈 리스트를 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberSessionDto.SessionResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(0);
                softly.assertThat(data.getTotalPages()).isEqualTo(0);
                softly.assertThat(data.getContent().size()).isEqualTo(0);
            });
        }
    }

    @Nested
    @DisplayName("특정 멤버 PT 내역 조회 테스트")
    public class MemberSessionDetailTest {
        private static final String MEMBER_SESSION_DETAIL_API = "/v1/members/{memberId}/sessions";

        @Test
        @DisplayName("회원 PT 내역 조회 성공 - 전체 조회")
        public void memberSessionSuccess() throws Exception {
            // given
            // 회원, 트레이너, 세션 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // session 7개 생성
            createSessions(member, trainer);

            // when
            // 트레이너가 회원 PT 내역을 조회한다면
            int page = 0;
            int size = 10;
            String url = MEMBER_SESSION_DETAIL_API.replace("{memberId}", member.getMemberId().toString()) + "?page=" + page + "&size=" + size;
            ExtractableResponse<Response> result = get(url, token);

            // then
            // PT 내역을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberSessionDto.SessionResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(7);
                softly.assertThat(data.getTotalPages()).isEqualTo(1);
                softly.assertThat(data.getContent().size()).isEqualTo(7);
            });
        }

        @Test
        @DisplayName("회원 PT 내역 조회 성공 - 상태별 조회")
        public void memberSessionSuccessByStatus() throws Exception {
            // given
            // 회원, 트레이너, 세션 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // session 7개 생성
            createSessions(member, trainer);

            // when
            // 트레이너가 회원 PT 내역을 상태별로 조회할 때
            int page = 0;
            int size = 10;
            String url = MEMBER_SESSION_DETAIL_API.replace("{memberId}", member.getMemberId().toString()) + "?page=" + page + "&size=" + size + "&status=SESSION_WAITING";
            ExtractableResponse<Response> result = get(url, token);

            // then
            // PT 내역을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberSessionDto.SessionResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(3);
                softly.assertThat(data.getTotalPages()).isEqualTo(1);
                softly.assertThat(data.getContent().size()).isEqualTo(3);
            });
        }

        @Test
        @DisplayName("회원 PT 내역 조회 성공 - 페이징 조회")
        public void memberSessionSuccessByPaging() throws Exception {
            // given
            // 회원, 트레이너, 세션 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // session 7개 생성
            createSessions(member, trainer);

            // when
            // 트레이너가 회원 PT 내역을 페이징 조회할 때
            int page = 0;
            int size = 3;
            String url = MEMBER_SESSION_DETAIL_API.replace("{memberId}", member.getMemberId().toString()) + "?page=" + page + "&size=" + size;
            ExtractableResponse<Response> result = get(url, token);

            // then
            // PT 내역을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberSessionDto.SessionResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(7);
                softly.assertThat(data.getTotalPages()).isEqualTo(3);
                softly.assertThat(data.getContent().size()).isEqualTo(3);
            });
        }

        @Test
        @DisplayName("회원 PT 내역 조회 성공 - 다른 트레이너와 진행한 PT 세션은 제외")
        public void memberSessionSuccessByAnotherTrainer() throws Exception {
            // given
            // 회원, 트레이너, 세션 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            Trainer anotherTrainer = testDataHandler.createTrainer("CD1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            createSessions(member, anotherTrainer);
            createSessions(member, trainer);

            // when
            // 트레이너가 회원 PT 내역을 조회할 때 다른 트레이너와 진행한 PT 세션은 제외한다
            int page = 0;
            int size = 10;
            String url = MEMBER_SESSION_DETAIL_API.replace("{memberId}", member.getMemberId().toString()) + "?page=" + page + "&size=" + size;
            ExtractableResponse<Response> result = get(url, token);

            // then
            // PT 내역을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberSessionDto.SessionResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberSessionDto.SessionResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(7);
                softly.assertThat(data.getTotalPages()).isEqualTo(1);
                softly.assertThat(data.getContent().size()).isEqualTo(7);
            });
        }


        @Test
        @DisplayName("회원 PT 내역 조회 실패 - 트레이너와 연결이 안되어있는 멤버일 때")
        public void memberSessionFailByNotConnected() throws Exception {
            // given
            // 회원이 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 연결되어 있지 않은 회원 PT 내역을 조회할 때
            String url = MEMBER_SESSION_DETAIL_API.replace("{memberId}", member.getMemberId().toString());
            ExtractableResponse<Response> result = get(url, token);

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
    @DisplayName("회원 PT 횟수 수정 테스트")
    public class MemberSessionCountUpdateTest {
        private static final String MEMBER_SESSION_COUNT_UPDATE_API = "/v1/members/{memberId}/session-info/{sessionInfoId}";

        @Test
        @DisplayName("회원 PT 횟수 수정 성공")
        public void memberSessionCountUpdateSuccess() throws Exception {
            // given
            // 회원, 트레이너 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            testDataHandler.connectMemberAndTrainer(member, trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            testDataHandler.createTokenInfo(member);
            testDataHandler.createTokenInfo(trainer);

            SessionInfo sessionInfo = testDataHandler.createSessionInfo(member, trainer);

            // when
            // 트레이너가 회원 PT 횟수 수정 요청을 보낸다면
            int remainingCount = 3;
            int totalCount = 5;
            String url = MEMBER_SESSION_COUNT_UPDATE_API.replace("{memberId}", member.getMemberId().toString())
                    .replace("{sessionInfoId}", sessionInfo.getSessionInfoId().toString());
            SessionInfoDto.UpdateRequest request = new SessionInfoDto.UpdateRequest(remainingCount, totalCount);
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = patch(url, requestBody, token);

            // then
            // PT 횟수가 수정된다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<SessionInfoDto.Response> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                SessionInfoDto.Response data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.sessionInfoId()).isEqualTo(sessionInfo.getSessionInfoId());
                softly.assertThat(data.remainingCount()).isEqualTo(remainingCount);
                softly.assertThat(data.totalCount()).isEqualTo(totalCount);

                SessionInfo updatedSessionInfo = sessionInfoRepository.getSessionInfo(sessionInfo.getSessionInfoId()).get();
                softly.assertThat(updatedSessionInfo.getRemainingCount()).isEqualTo(remainingCount);
                softly.assertThat(updatedSessionInfo.getTotalCount()).isEqualTo(totalCount);

                // 세션 수정했다는 알림 보냈는지 확인
                List<Notification> notifications = notificationRepository.getNotification(data.sessionInfoId(), Notification.ReferenceType.SESSION);
                softly.assertThat(notifications.get(0).getContent()).contains("트레이너가 회원님의 세션 정보를 수정하였습니다.");
            });
        }

        @Test
        @DisplayName("회원 PT 횟수 수정 실패 - 트레이너가 회원과 연결이 안되어 있을 때")
        public void memberSessionCountUpdateFailByNotConnected() throws Exception {
            // given
            // 회원, 트레이너 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            SessionInfo sessionInfo = testDataHandler.createSessionInfo(member, trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 회원과 연결이 안되어 있는 회원 PT 횟수 수정 요청을 보낸다면
            int remainingCount = 3;
            int totalCount = 5;
            String url = MEMBER_SESSION_COUNT_UPDATE_API.replace("{memberId}", member.getMemberId().toString())
                    .replace("{sessionInfoId}", sessionInfo.getSessionInfoId().toString());
            SessionInfoDto.UpdateRequest request = new SessionInfoDto.UpdateRequest(remainingCount, totalCount);
            String requestBody = writeValueAsString(request);
            ExtractableResponse<Response> result = patch(url, requestBody, token);

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
    @DisplayName("내 회원 리스트 조회 api 테스트")
    public class MemberListTest {
        private static final String MEMBER_LIST_API = "/v1/members";

        @Test
        @DisplayName("내 회원 리스트 조회 성공")
        public void memberListSuccess() throws Exception {
            // given
            // 트레이너, 회원 정보가 있을 때
            Trainer trainer = testDataHandler.createTrainer("AB1423");

            // 7명의 회원 생성
            createMembers(trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 회원 리스트를 조회할 때
            String url = MEMBER_LIST_API;
            ExtractableResponse<Response> result = get(url, token);

            // then
            // 회원 리스트를 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberInfoDto.SimpleResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberInfoDto.SimpleResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(7);
                softly.assertThat(data.getTotalPages()).isEqualTo(1);
                softly.assertThat(data.getContent().size()).isEqualTo(7);
            });
        }

        @Test
        @DisplayName("내 회원 리스트 조회 성공 - 페이징 조회")
        public void memberListSuccessByPaging() throws Exception {
            // given
            // 트레이너, 회원 정보가 있을 때
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            // 7명의 회원 생성
            createMembers(trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 회원 리스트를 페이징 조회할 때
            int page = 0;
            int size = 3;
            String url = MEMBER_LIST_API + "?page=" + page + "&size=" + size;
            ExtractableResponse<Response> result = get(url, token);

            // then
            // 회원 리스트를 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberInfoDto.SimpleResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberInfoDto.SimpleResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(7);
                softly.assertThat(data.getTotalPages()).isEqualTo(3);
                softly.assertThat(data.getContent().size()).isEqualTo(3);
            });
        }

        @Test
        @DisplayName("내 회원 리스트 조회 성공 - 키워드 검색")
        public void memberListSuccessByKeyword() throws Exception {
            // given
            // 트레이너, 회원 정보가 있을 때
            Trainer trainer = testDataHandler.createTrainer("AB1423");

            // 7명의 회원 생성
            createMembers(trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 회원 리스트를 키워드로 검색할 때
            String keyword = "member1";
            String url = MEMBER_LIST_API + "?q=" + keyword;
            ExtractableResponse<Response> result = get(url, token);

            // then
            // 회원 리스트를 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<CustomPageResponse<MemberInfoDto.SimpleResponse>> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                CustomPageResponse<MemberInfoDto.SimpleResponse> data = response.data();
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.success()).isTrue();
                softly.assertThat(response.status()).isEqualTo(200);

                softly.assertThat(data.getTotalElements()).isEqualTo(1);
                softly.assertThat(data.getTotalPages()).isEqualTo(1);
                softly.assertThat(data.getContent().size()).isEqualTo(1);
            });
        }
    }

    @Nested
    @DisplayName("내 특정 회원 조회 테스트")
    public class MyMemberInfoTest {
        private static final String MEMBER_INFO_API = "/v1/members/{memberId}";

        @Test
        @DisplayName("회원 정보 조회 성공")
        public void memberInfoSuccess() throws Exception {
            // given
            // 회원, 트레이너, 세션 정보, PT 희망 시간 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            SessionInfo sessionInfo = testDataHandler.createSessionInfo(member, trainer);
            String token = testDataHandler.createTokenFromTrainer(trainer);
            testDataHandler.connectMemberAndTrainer(member, trainer);
            List<WorkoutSchedule> workoutSchedules = testDataHandler.createWorkoutSchedules(member);

            // when
            // 트레이너가 자기 회원의 정보를 조회한다면
            String url = MEMBER_INFO_API.replace("{memberId}", member.getMemberId().toString());
            ExtractableResponse<Response> result = get(url, token);

            // then
            // 회원의 정보를 받는다
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

                softly.assertThat(data.workoutSchedules().size()).isEqualTo(workoutSchedules.size());
                for (WorkoutScheduleDto.Response responseDto : data.workoutSchedules()) {
                    WorkoutSchedule workoutSchedule = workoutSchedules.stream().filter(ws -> ws.getWorkoutScheduleId()
                            .equals(responseDto.workoutScheduleId())).findFirst().orElseThrow();

                    softly.assertThat(responseDto.workoutScheduleId()).isEqualTo(workoutSchedule.getWorkoutScheduleId());
                    softly.assertThat(responseDto.dayOfWeek()).isEqualTo(workoutSchedule.getDayOfWeek());
                    softly.assertThat(responseDto.preferenceTimes())
                            .containsExactlyInAnyOrderElementsOf(workoutSchedule.getPreferenceTimes());
                }
            });
        }

        @Test
        @DisplayName("회원 정보 조회 실패 - 트레이너와 연결이 안되어있는 멤버일 때")
        public void memberInfoFailByNotConnected() throws Exception {
            // given
            // 회원, 트레이너 정보가 있을 때
            Member member = testDataHandler.createMember();
            Trainer trainer = testDataHandler.createTrainer("AB1423");
            String token = testDataHandler.createTokenFromTrainer(trainer);

            // when
            // 트레이너가 연결되어 있지 않은 회원 정보를 조회할 때
            String url = MEMBER_INFO_API.replace("{memberId}", member.getMemberId().toString());
            ExtractableResponse<Response> result = get(url, token);

            // then
            // 연결 정보가 없다는 응답을 받는다
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                ApiResultResponse<Object> response = readValue(result.body().jsonPath().prettify(), new TypeReference<>() {
                });

                softly.assertThat(response).isNotNull();
                softly.assertThat(response.msg()).isEqualTo("트레이너가 멤버와 연결되어 있지 않습니다.");
                softly.assertThat(response.success()).isFalse();
                softly.assertThat(response.status()).isEqualTo(400);
                softly.assertThat(response.data()).isNull();
            });
        }
    }

    private void createMembers(Trainer trainer) {
        Member member1 = testDataHandler.createMember("member1");
        Member member2 = testDataHandler.createMember("member2");
        Member member3 = testDataHandler.createMember("member3");
        Member member4 = testDataHandler.createMember("member4");
        Member member5 = testDataHandler.createMember("member5");
        Member member6 = testDataHandler.createMember("member6");
        Member member7 = testDataHandler.createMember("member7");

        // 2명의 회원은 미연결
        testDataHandler.createMember("member8");
        testDataHandler.createMember("member9");


        testDataHandler.connectMemberAndTrainer(member1, trainer);
        testDataHandler.connectMemberAndTrainer(member2, trainer);
        testDataHandler.connectMemberAndTrainer(member3, trainer);
        testDataHandler.connectMemberAndTrainer(member4, trainer);
        testDataHandler.connectMemberAndTrainer(member5, trainer);
        testDataHandler.connectMemberAndTrainer(member6, trainer);
        testDataHandler.connectMemberAndTrainer(member7, trainer);
    }

    private void createSessions(Member member, Trainer trainer) {
        testDataHandler.createSession(member, trainer, Session.Status.SESSION_COMPLETED);
        testDataHandler.createSession(member, trainer, Session.Status.SESSION_COMPLETED);
        testDataHandler.createSession(member, trainer, Session.Status.SESSION_WAITING);
        testDataHandler.createSession(member, trainer, Session.Status.SESSION_WAITING);
        testDataHandler.createSession(member, trainer, Session.Status.SESSION_WAITING);
        testDataHandler.createSession(member, trainer, Session.Status.SESSION_NOT_ATTEND);
        testDataHandler.createSession(member, trainer, Session.Status.SESSION_NOT_ATTEND);
    }


}
