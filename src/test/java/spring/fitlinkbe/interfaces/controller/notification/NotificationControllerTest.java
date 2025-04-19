package spring.fitlinkbe.interfaces.controller.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import spring.fitlinkbe.application.notification.NotificationFacade;
import spring.fitlinkbe.domain.common.PersonalDetailRepository;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.interfaces.controller.notification.dto.NotificationRequestDto;
import spring.fitlinkbe.support.argumentresolver.LoginMemberArgumentResolver;
import spring.fitlinkbe.support.security.AuthTokenProvider;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationFacade notificationFacade;

    @MockitoBean
    private AuthTokenProvider authTokenProvider;

    @MockitoBean
    private PersonalDetailRepository personalDetailRepository;

    @MockitoBean
    private LoginMemberArgumentResolver loginMemberArgumentResolver;

    @Nested
    @DisplayName("알림 목록 조회 Controller TEST")
    class GetNotificationsControllerTest {
        @Test
        @DisplayName("트레이너가 알림 목록 조회 성공")
        void getNotificationsWithTrainer() throws Exception {
            //given
            Pageable pageRequest = PageRequest.of(0, 10);
            Notification.ReferenceType category = Notification.ReferenceType.CONNECT;

            Notification notification1 = Notification.builder()
                    .notificationId(1L)
                    .refType(Notification.ReferenceType.SESSION)
                    .notificationType(Notification.NotificationType.SESSION_COMPLETED)
                    .content("알림1")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();
            Notification notification2 = Notification.builder()
                    .notificationId(2L)
                    .refType(Notification.ReferenceType.SESSION)
                    .notificationType(Notification.NotificationType.SESSION_COMPLETED)
                    .content("알림2")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();
            Notification notification3 = Notification.builder()
                    .notificationId(3L)
                    .refType(Notification.ReferenceType.SESSION)
                    .notificationType(Notification.NotificationType.SESSION_COMPLETED)
                    .content("알림3")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();

            List<Notification> notifications = List.of(notification1, notification2, notification3);
            Page<Notification> response = new PageImpl<>(notifications, pageRequest, notifications.size());


            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(notificationFacade.getNotifications(any(Notification.ReferenceType.class), any(PageRequest.class)
                    , any(SecurityUser.class), any())).thenReturn(response);

            //when & then
            mockMvc.perform(get("/v1/notifications")
                            .queryParam("type", category.toString())
                            .queryParam("pageRequest", pageRequest.toString())
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
        @DisplayName("멤버가 알림 목록 조회 성공")
        void getNotificationsWithMember() throws Exception {
            //given
            Pageable pageRequest = PageRequest.of(0, 10);
            Notification.ReferenceType category = Notification.ReferenceType.CONNECT;

            Notification notification1 = Notification.builder()
                    .notificationId(1L)
                    .refType(Notification.ReferenceType.CONNECT)
                    .notificationType(Notification.NotificationType.CONNECT)
                    .content("알림1")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();
            Notification notification2 = Notification.builder()
                    .notificationId(2L)
                    .refType(Notification.ReferenceType.CONNECT)
                    .notificationType(Notification.NotificationType.CONNECT)
                    .content("알림2")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();
            Notification notification3 = Notification.builder()
                    .notificationId(3L)
                    .refType(Notification.ReferenceType.CONNECT)
                    .notificationType(Notification.NotificationType.CONNECT)
                    .content("알림3")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();

            List<Notification> notifications = List.of(notification1, notification2, notification3);
            Page<Notification> response = new PageImpl<>(notifications, pageRequest, notifications.size());


            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("멤버1")
                    .memberId(1L)
                    .trainerId(null)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(notificationFacade.getNotifications(any(Notification.ReferenceType.class), any(PageRequest.class)
                    , any(SecurityUser.class), any())).thenReturn(response);

            //when & then
            mockMvc.perform(get("/v1/notifications")
                            .queryParam("type", category.toString())
                            .queryParam("pageRequest", pageRequest.toString())
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
        @DisplayName("트레이너가 알림 목록 조회 실패 - 존재하지 않는 카테고리 타입 전달")
        void getNotificationsWithTrainerNoSpecificCategory() throws Exception {
            //given
            Pageable pageRequest = PageRequest.of(0, 10);
            String category = "카테고리";

            Notification notification1 = Notification.builder()
                    .notificationId(1L)
                    .content("알림1")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();
            Notification notification2 = Notification.builder()
                    .notificationId(2L)
                    .content("알림2")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();
            Notification notification3 = Notification.builder()
                    .notificationId(3L)
                    .content("알림3")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();

            List<Notification> notifications = List.of(notification1, notification2, notification3);
            Page<Notification> response = new PageImpl<>(notifications, pageRequest, notifications.size());


            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            when(notificationFacade.getNotifications(any(Notification.ReferenceType.class), any(PageRequest.class)
                    , any(SecurityUser.class), any(String.class))).thenReturn(response);

            //when & then
            mockMvc.perform(get("/v1/notifications")
                            .queryParam("type", category)
                            .queryParam("pageRequest", pageRequest.toString())
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("요청 파라미터 'type'의 값 '카테고리'은(는) 유효하지 않습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("알림 상세 조회 Controller TEST")
    class GetNotificationDetailControllerTest {
        @Test
        @DisplayName("트레이너가 알림 상세 조회 - 성공")
        void getNotificationDetail() throws Exception {
            //given
            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            Long notificationId = 1L;

            Notification notification = Notification.builder()
                    .notificationId(notificationId)
                    .refType(Notification.ReferenceType.SESSION)
                    .personalDetail(personalDetail)
                    .notificationType(Notification.NotificationType.SESSION_COMPLETED)
                    .content("알림1")
                    .sendDate(LocalDateTime.now())
                    .isProcessed(false)
                    .build();

            when(notificationFacade.getNotificationDetail(any(Long.class), any(SecurityUser.class)))
                    .thenReturn(notification);

            //when & then
            mockMvc.perform(get("/v1/notifications/%s".formatted(notificationId))
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user)))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.msg").value("OK"))
                    .andExpect(jsonPath("$.data").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("푸쉬 토큰 등록 Controller TEST")
    class RegisterPushTokenControllerTest {

        @Test
        @DisplayName("푸쉬 토큰 등록 - 성공")
        void registerPushToken() throws Exception {
            //given
            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            NotificationRequestDto.PushTokenRequest request = NotificationRequestDto
                    .PushTokenRequest
                    .builder()
                    .pushToken("push-token")
                    .build();

            doNothing().when(notificationFacade)
                    .registerPushToken(request.toCriteria(), user);

            //when & then
            mockMvc.perform(post("/v1/notifications/push-token/register")
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
                    .andExpect(jsonPath("$.data.message").value("푸쉬 토큰 등록에 성공하였습니다."));
        }

        @Test
        @DisplayName("푸쉬 토큰 등록 - 실패: 토큰 없음")
        void registerPushTokenNoToken() throws Exception {
            //given
            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            String accessToken = getAccessToken(personalDetail);

            NotificationRequestDto.PushTokenRequest request = NotificationRequestDto
                    .PushTokenRequest
                    .builder()
                    .build();

            doNothing().when(notificationFacade)
                    .registerPushToken(request.toCriteria(), user);

            //when & then
            mockMvc.perform(post("/v1/notifications/push-token/register")
                            .header("Authorization", "Bearer " + accessToken)
                            .with(oauth2Login().oauth2User(user))
                            .with(csrf())
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))  // OAuth2 인증
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.msg").value("푸쉬 토큰은 필수값 입니다."))
                    .andExpect(jsonPath("$.data").doesNotExist());
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
