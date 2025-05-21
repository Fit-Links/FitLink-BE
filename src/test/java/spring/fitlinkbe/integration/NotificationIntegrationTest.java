package spring.fitlinkbe.integration;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.TokenRepository;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.member.MemberRepository;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationRepository;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerRepository;
import spring.fitlinkbe.integration.common.BaseIntegrationTest;
import spring.fitlinkbe.integration.common.TestDataHandler;
import spring.fitlinkbe.interfaces.controller.notification.dto.NotificationRequestDto;
import spring.fitlinkbe.interfaces.controller.notification.dto.NotificationResponseDto;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationRequestDto;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationResponseDto;
import spring.fitlinkbe.support.security.AuthTokenProvider;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.NOTIFICATION_NOT_FOUND;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.FIXED_RESERVATION;

public class NotificationIntegrationTest extends BaseIntegrationTest {

    private static final String PATH = "/v1/notifications";

    @Autowired
    AuthTokenProvider tokenProvider;

    @Autowired
    PersonalDetailRepository personalDetailRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TrainerRepository trainerRepository;

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    TestDataHandler testDataHandler;

    @BeforeEach
    void setUp() {
        testDataHandler.settingUserInfo();
        testDataHandler.settingSessionInfo();
        testDataHandler.createTokenInfo();
        testDataHandler.createMember();
    }

    @Nested
    @DisplayName("알림 전송 Integration TEST")
    class SendNotificationIntegrationTest {
        @Test
        @DisplayName("예약 성공 시 알림 전송 - 성공")
        void sendNotificationWhenSessionReserved() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .memberId(1L)
                    .name("홍길동")
                    .dates(List.of(requestDate))
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + "/v1/reservations/fixed-reservations",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                // 예약이 잘 됐는지 확인
                softly.assertThat(result.statusCode()).isEqualTo(200);

                List<ReservationResponseDto.Success> content = result.body().jsonPath()
                        .getList("data", ReservationResponseDto.Success.class);

                softly.assertThat(content.get(0).reservationId()).isEqualTo(1L);
                softly.assertThat(content.get(0).status()).isEqualTo(FIXED_RESERVATION.getName());

                // 알림이 잘 생성됐는지 확인
                PersonalDetail memberDetail = personalDetailRepository.getMemberDetail(1L).orElseThrow();

                Notification notification = notificationRepository.getNotification(memberDetail.getPersonalDetailId());
                softly.assertThat(notification).isNotNull();
                softly.assertThat(notification.getContent()).contains("김민수 회원님의 예약이 확정되었습니다.");

            });
        }


        @Test
        @DisplayName("예약 실패 시 알림 전송 - 실패")
        void sendNotificationWhenSessionReservationFailed() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            LocalDateTime requestDate = LocalDateTime.now().plusHours(1);

            ReservationRequestDto.CreateFixed request = ReservationRequestDto.CreateFixed.builder()
                    .name("홍길동")
                    .dates(List.of(requestDate))
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + "/v1/reservations/fixed-reservations",
                    request,
                    accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).isEqualTo("유저 ID는 필수값 입니다.");
                softly.assertThat(result.body().jsonPath().getList("data", ReservationResponseDto.Success.class))
                        .isEmpty();
            });

            // 알림 생성 실패 확인
            PersonalDetail memberDetail = personalDetailRepository.getMemberDetail(1L).orElseThrow();

            assertThatThrownBy(() -> notificationRepository.getNotification(memberDetail.getPersonalDetailId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(NOTIFICATION_NOT_FOUND);
        }
    }


    @Nested
    @DisplayName("알림 목록 조회 Integration TEST")
    class SearchConditionIntegrationTest {
        @Test
        @DisplayName("트레이너 알림 목록 조회 - 성공 : 전부 조회")
        void getNotificationsWithAllType() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Member member = memberRepository.getMember(1L).orElseThrow();

            UserRole userRole = UserRole.TRAINER;

            // 알림 20개 저장
            createNotifications(personalDetail, member.getMemberId(), userRole);

            Map<String, String> params = new HashMap<>();
            params.put("page", "0");
            params.put("size", "10");

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<NotificationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data.content", NotificationResponseDto.Summary.class);
                softly.assertThat(content.get(0).notificationId()).isEqualTo(1);
                softly.assertThat(content.size()).isEqualTo(10);
                Boolean hasNext = result.body().jsonPath().getBoolean("data.hasNext");
                softly.assertThat(hasNext).isTrue();
            });
        }

        @Test
        @DisplayName("트레이너 알림 목록 조회 - 성공 : 검색어 입력")
        void getNotificationsWithKeyword() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Member member = memberRepository.getMember(1L).orElseThrow();

            UserRole userRole = UserRole.TRAINER;

            // 알림 20개 저장
            createNotifications(personalDetail, member.getMemberId(), userRole);

            Notification.ReferenceType refType = Notification.ReferenceType.RESERVATION_REQUEST;

            Map<String, String> params = new HashMap<>();
            params.put("page", "0");
            params.put("size", "10");
            params.put("type", refType.name());
            params.put("keyword", "예약");

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<NotificationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data.content", NotificationResponseDto.Summary.class);
                softly.assertThat(content.get(0).notificationId()).isEqualTo(1);
                softly.assertThat(content.size()).isEqualTo(5);
                Boolean hasNext = result.body().jsonPath().getBoolean("data.hasNext");
                softly.assertThat(hasNext).isFalse();
            });
        }

        @Test
        @DisplayName("트레이너 알림 목록 조회 - 성공 : session 타입 조회")
        void getNotificationsWithSessionType() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Member member = memberRepository.getMember(1L).orElseThrow();

            UserRole userRole = UserRole.TRAINER;

            // 알림 20개 저장
            createNotifications(personalDetail, member.getMemberId(), userRole);

            Notification.ReferenceType refType = Notification.ReferenceType.SESSION;

            Map<String, String> params = new HashMap<>();
            params.put("page", "0");
            params.put("size", "10");
            params.put("type", refType.name());

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<NotificationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data.content", NotificationResponseDto.Summary.class);
                softly.assertThat(content.get(0).notificationId()).isEqualTo(6);
                softly.assertThat(content.size()).isEqualTo(10);
                Boolean hasNext = result.body().jsonPath().getBoolean("data.hasNext");
                softly.assertThat(hasNext).isTrue();
            });
        }

        @Test
        @DisplayName("트레이너 알림 목록 조회 - 성공 : 특정 멤버를 검색할 때")
        void getNotificationsWithTrainerWhenTargetMemberGiven() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Member member = memberRepository.getMember(1L).orElseThrow();
            Member member2 = memberRepository.getMember(2L).orElseThrow();

            UserRole userRole = UserRole.TRAINER;

            // 알림 20개 저장
            createNotifications(personalDetail, member.getMemberId(), userRole);
            // 새로운 알림 2개 추가
            createNotification(personalDetail, member2.getMemberId(), userRole);
            createNotification(personalDetail, member2.getMemberId(), userRole);

            Map<String, String> params = new HashMap<>();
            params.put("page", "0");
            params.put("size", "10");
            params.put("memberId", "2");

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<NotificationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data.content", NotificationResponseDto.Summary.class);
                softly.assertThat(content.size()).isEqualTo(2);
                Boolean hasNext = result.body().jsonPath().getBoolean("data.hasNext");
                softly.assertThat(hasNext).isFalse();
            });
        }

        @Test
        @DisplayName("멤버 알림 목록 조회 - 성공")
        void getNotificationsWithMember() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            UserRole userRole = UserRole.MEMBER;

            // 알림 20개 저장
            createNotifications(personalDetail, trainer.getTrainerId(), userRole);

            Map<String, String> params = new HashMap<>();
            params.put("page", "0");
            params.put("size", "10");

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                List<NotificationResponseDto.Summary> content = result.body().jsonPath()
                        .getList("data.content", NotificationResponseDto.Summary.class);
                softly.assertThat(content.get(0).notificationId()).isEqualTo(1);
                softly.assertThat(content.size()).isEqualTo(10);
                Boolean hasNext = result.body().jsonPath().getBoolean("data.hasNext");
                softly.assertThat(hasNext).isTrue();
            });
        }

        @Test
        @DisplayName("알림 목록 조회 - 실패 : 잘못된 타입을 줬을 때")
        void getNotificationsWithStrangeType() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            UserRole userRole = UserRole.MEMBER;

            // 알림 20개 저장
            createNotifications(personalDetail, trainer.getTrainerId(), userRole);

            Notification.NotificationType refType = Notification.NotificationType.SESSION_REMINDER;

            Map<String, String> params = new HashMap<>();
            params.put("page", "0");
            params.put("size", "10");
            params.put("type", refType.name());

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH, params, accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("요청 파라미터 'type'의 값 'SESSION_REMINDER'은(는) 유효하지 않습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", NotificationResponseDto.class)).isNull();
            });
        }

    }

    @Nested
    @DisplayName("알림 상세 조회 Integration TEST")
    class GetNotificationDetailIntegrationTest {
        @Test
        @DisplayName("트레이너 알림 상세 조회 - 성공")
        void getNotificationDetailWithTrainer() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Member member = memberRepository.getMember(1L).orElseThrow();

            UserRole userRole = UserRole.TRAINER;

            // 알림 20개 저장
            createNotifications(personalDetail, member.getMemberId(), userRole);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/1", accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                NotificationResponseDto.Detail content = result.body().jsonPath()
                        .getObject("data", NotificationResponseDto.Detail.class);

                softly.assertThat(content.notificationId()).isEqualTo(1L);
                softly.assertThat(content.content()).contains("예약");
                softly.assertThat(content.userDetail().name()).contains("김민수"); //회원 이름
                softly.assertThat(content.userDetail()).isNotNull();
            });
        }

        @Test
        @DisplayName("멤버 알림 상세 조회 - 성공")
        void getNotificationDetailWithMember() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Trainer trainer = trainerRepository.getTrainerInfo(1L).orElseThrow();

            UserRole userRole = UserRole.MEMBER;

            // 알림 20개 저장
            createNotifications(personalDetail, trainer.getTrainerId(), userRole);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/1", accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                NotificationResponseDto.Detail content = result.body().jsonPath()
                        .getObject("data", NotificationResponseDto.Detail.class);

                softly.assertThat(content.notificationId()).isEqualTo(1L);
                softly.assertThat(content.content()).contains("예약");
                softly.assertThat(content.userDetail().name()).contains("트레이너황"); //트레이너 이름
                softly.assertThat(content.userDetail()).isNotNull();
            });
        }

        @Test
        @DisplayName("알림 상세 조회 - 실패: 없는 알림 ID 조회")
        void getNotificationDetailWithNoNotificationId() {
            // given
            PersonalDetail personalDetail = personalDetailRepository.getTrainerDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    personalDetail.getPersonalDetailId(), personalDetail.getUserRole());

            Member member = memberRepository.getMember(1L).orElseThrow();

            UserRole userRole = UserRole.TRAINER;

            // 알림 20개 저장
            createNotifications(personalDetail, member.getMemberId(), userRole);

            // when
            ExtractableResponse<Response> result = get(LOCAL_HOST + port + PATH + "/100", accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Number.class)).isEqualTo(404);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isFalse();
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class))
                        .contains("알림 정보를 찾지 못하였습니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", NotificationResponseDto.Detail.class))
                        .isNull();
            });
        }
    }


    @Nested
    @DisplayName("푸쉬 토큰 등록 Integration TEST")
    class RegisterPushTokenIntegrationTest {
        @Test
        @DisplayName("푸쉬 토큰 등록 - 성공")
        void registerPushToken() {
            // given
            PersonalDetail memberDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    memberDetail.getPersonalDetailId(), memberDetail.getUserRole());

            String pushToken = "push-token";
            NotificationRequestDto.PushTokenRequest request = NotificationRequestDto.PushTokenRequest.builder()
                    .pushToken(pushToken)
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/push-token/register", request
                    , accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                NotificationResponseDto.PushToken content = result.body().jsonPath()
                        .getObject("data", NotificationResponseDto.PushToken.class);

                softly.assertThat(content.message()).isEqualTo("푸쉬 토큰 등록에 성공하였습니다.");

                // pushToken이 잘 저장되었는지 확인
                Token token = tokenRepository.getByPersonalDetailId(memberDetail.getPersonalDetailId()).orElseThrow();

                softly.assertThat(token.getPushToken()).isEqualTo(pushToken);
            });
        }

        @Test
        @DisplayName("푸쉬 토큰 등록 - 실패: 토큰 없음")
        void registerPushTokenWithNoToken() {
            // given
            PersonalDetail memberDetail = personalDetailRepository.getMemberDetail(1L)
                    .orElseThrow();

            String accessToken = tokenProvider.createAccessToken(PersonalDetail.Status.NORMAL,
                    memberDetail.getPersonalDetailId(), memberDetail.getUserRole());

            NotificationRequestDto.PushTokenRequest request = NotificationRequestDto.PushTokenRequest.builder()
                    .build();

            // when
            ExtractableResponse<Response> result = post(LOCAL_HOST + port + PATH + "/push-token/register", request
                    , accessToken);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.statusCode()).isEqualTo(200);
                softly.assertThat(result.body().jsonPath().getObject("status", Integer.class)).isEqualTo(400);
                softly.assertThat(result.body().jsonPath().getObject("success", Boolean.class)).isEqualTo(false);
                softly.assertThat(result.body().jsonPath().getObject("msg", String.class)).contains("푸쉬 토큰은 필수값 입니다.");
                softly.assertThat(result.body().jsonPath().getObject("data", ReservationResponseDto.Success.class)).isNull();
            });
        }
    }


    private void createNotifications(PersonalDetail personalDetail, Long partnerId, UserRole userRole) {
        for (int i = 0; i < 20; i++) {
            Notification notification;
            if (i < 5) {
                notification = Notification.builder()
                        .refId((long) i)
                        .refType(Notification.ReferenceType.RESERVATION_REQUEST)
                        .target(userRole)
                        .notificationType(Notification.NotificationType.RESERVATION_APPROVE)
                        .personalDetail(personalDetail)
                        .partnerId(partnerId)
                        .name("알림 " + i)
                        .content("예약 내용 " + i)
                        .sendDate(LocalDateTime.now().minusDays(i))
                        .build();

            } else {
                notification = Notification.builder()
                        .refId((long) i)
                        .refType(Notification.ReferenceType.SESSION)
                        .target(userRole)
                        .notificationType(Notification.NotificationType.SESSION_COMPLETED)
                        .personalDetail(personalDetail)
                        .partnerId(partnerId)
                        .name("알림 " + i)
                        .content("세션 내용 " + i)
                        .sendDate(LocalDateTime.now().minusDays(i))
                        .build();

            }
            notificationRepository.save(notification);
        }
    }

    private void createNotification(PersonalDetail personalDetail, Long partnerId, UserRole userRole) {
        Notification notification = Notification.builder()
                .refId((long) 1)
                .refType(Notification.ReferenceType.RESERVATION_REQUEST)
                .target(userRole)
                .notificationType(Notification.NotificationType.RESERVATION_APPROVE)
                .personalDetail(personalDetail)
                .partnerId(partnerId)
                .name(Notification.NotificationType.RESERVATION_APPROVE.name())
                .content("예약이 확정되었습니다.")
                .sendDate(LocalDateTime.now().minusDays(1))
                .build();

        notificationRepository.save(notification);
    }

}
