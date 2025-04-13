package spring.fitlinkbe.domain.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.notification.command.NotificationCommand;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationStrategyHandler strategyHandler;

    @Mock
    private FcmService fcmService;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("알림 전송 Service TEST")
    class SendNotificationServiceTest {

        @Test
        @DisplayName("연동 요청 알림 전송 - 성공")
        void sendNotificationWithConnect() {
            //given
            String memberName = "멤버1";
            PersonalDetail personalDetail = PersonalDetail.builder().build();
            NotificationCommand.Connect connectDto = NotificationCommand.Connect.of(personalDetail,
                    1L, memberName, 1L);

            Notification notification = Notification.builder()
                    .notificationId(1L)
                    .refId(1L)
                    .refType(Notification.ReferenceType.CONNECT)
                    .target(UserRole.TRAINER)
                    .notificationType(Notification.NotificationType.CONNECT)
                    .personalDetail(personalDetail)
                    .partnerId(1L)
                    .name(Notification.NotificationType.CONNECT.getName())
                    .content(memberName + " 님에게 연동 요청이 왔습니다.")
                    .sendDate(LocalDateTime.now())
                    .build();

            when(strategyHandler.handle(connectDto)).thenReturn(notification);
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

            //when
            notificationService.sendNotification(connectDto);

            //then
            verify(strategyHandler).handle(connectDto);
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("연동 요청 알림 보내기 - 실패: 이상한 타입 예외 발생")
        void sendNotificationStrategyHandler() {
            // given
            NotificationCommand.Connect connectDto = mock(NotificationCommand.Connect.class);
            when(strategyHandler.handle(connectDto)).thenThrow(new IllegalArgumentException("지원하지 않는 타입"));

            // when & then
            assertThatThrownBy(() -> notificationService.sendNotification(connectDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("지원하지 않는 타입");

            verify(strategyHandler).handle(connectDto);
            verifyNoInteractions(notificationRepository);
        }
    }

    @Nested
    @DisplayName("알림 보내기 Service TEST")
    class GetNotificationsServiceTest {


        @DisplayName("알림 목록 조회 - 성공")
        @Test
        void getNotifications() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Notification.ReferenceType refType = Notification.ReferenceType.CONNECT;
            String keyword = "멤버1";

            NotificationCommand.GetNotifications command = new NotificationCommand.GetNotifications(refType,
                    pageable, keyword);
            SecurityUser user = mock(SecurityUser.class);

            when(user.getUserRole()).thenReturn(UserRole.TRAINER);
            when(user.getPersonalDetailId()).thenReturn(1L);

            List<Notification> content = List.of(Notification.builder().notificationId(1L).build());
            Page<Notification> expectedPage = new PageImpl<>(content, pageable, 1L);

            when(notificationRepository.getNotifications(refType, pageable, UserRole.TRAINER, 1L,
                    keyword))
                    .thenReturn(expectedPage);

            // when
            Page<Notification> result = notificationService.getNotifications(command, user);

            // then
            assertThat(result).isEqualTo(expectedPage);
            verify(notificationRepository).getNotifications(refType, pageable, UserRole.TRAINER, 1L,
                    keyword);
        }

        @DisplayName("알림 목록 조회 - 실패: repository 예외 발생")
        @Test
        void getNotificationsWithRepositoryError() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "멤버1";
            Notification.ReferenceType refType = Notification.ReferenceType.CONNECT;
            NotificationCommand.GetNotifications command = new NotificationCommand.GetNotifications(refType, pageable,
                    keyword);
            SecurityUser user = mock(SecurityUser.class);

            when(user.getUserRole()).thenReturn(UserRole.TRAINER);
            when(user.getPersonalDetailId()).thenReturn(1L);

            when(notificationRepository.getNotifications(any(), any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("DB 오류"));

            // when & then
            assertThatThrownBy(() -> notificationService.getNotifications(command, user))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB 오류");
        }
    }
}
