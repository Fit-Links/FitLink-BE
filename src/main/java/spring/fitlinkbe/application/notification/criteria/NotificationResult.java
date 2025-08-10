package spring.fitlinkbe.application.notification.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.notification.Notification;


public class NotificationResult {
    @Builder(toBuilder = true)
    public record NotificationDetail(Notification notification, PersonalDetail personalDetail) {
        public static NotificationResult.NotificationDetail from(Notification notification, PersonalDetail personalDetail) {

            return NotificationDetail.builder()
                    .notification(notification)
                    .personalDetail(personalDetail)
                    .build();
        }
    }
}
