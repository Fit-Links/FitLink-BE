package spring.fitlinkbe.application.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.application.notification.criteria.NotificationCriteria;
import spring.fitlinkbe.application.notification.criteria.NotificationResult;
import spring.fitlinkbe.domain.auth.AuthService;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.notification.command.NotificationCommand;
import spring.fitlinkbe.domain.trainer.TrainerService;
import spring.fitlinkbe.support.security.SecurityUser;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationService notificationService;
    private final TrainerService trainerService;
    private final MemberService memberService;
    private final AuthService authService;

    public Page<Notification> getNotifications(NotificationCriteria.SearchCondition criteria,
                                               SecurityUser user) {
        Notification.ReferenceType type = criteria.type();
        Pageable pageRequest = criteria.pageRequest();
        String keyword = criteria.q();
        Long memberId = criteria.memberId();

        NotificationCommand.SearchCondition command = NotificationCommand.SearchCondition.builder()
                .type(type)
                .pageRequest(pageRequest)
                .keyword(keyword)
                .memberId(memberId)
                .build();

        return notificationService.getNotifications(command, user);
    }

    public NotificationResult.NotificationDetail getNotificationDetail(Long notificationId, SecurityUser user) {
        Notification notification = notificationService.getNotificationDetail(notificationId, user);
        if (user.getUserRole() == UserRole.TRAINER) {
            PersonalDetail memberDetail = memberService.getMemberDetail(notification.getPartnerId());

            return NotificationResult.NotificationDetail.from(notification, memberDetail);
        }

        PersonalDetail trainerDetail = trainerService.getTrainerDetail(notification.getPartnerId());

        return NotificationResult.NotificationDetail.from(notification, trainerDetail);
    }

    public void registerPushToken(NotificationCriteria.PushTokenRequest criteria, SecurityUser user) {
        authService.registerPushToken(criteria.toCommand(), user);
    }
}
