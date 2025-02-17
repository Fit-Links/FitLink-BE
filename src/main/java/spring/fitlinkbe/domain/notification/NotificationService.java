package spring.fitlinkbe.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void sendConnectRequestNotification(PersonalDetail trainerDetail, String memberName, Long connectingInfoId) {
        Notification notification = Notification.connectRequestNotification(trainerDetail, memberName, connectingInfoId);
        notificationRepository.save(notification);
    }
}
