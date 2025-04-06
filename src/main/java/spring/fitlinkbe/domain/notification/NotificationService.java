package spring.fitlinkbe.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.time.LocalDateTime;

import static spring.fitlinkbe.domain.notification.Notification.*;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void sendConnectRequestNotification(PersonalDetail trainerDetail, Long memberId, String memberName,
                                               Long connectingInfoId) {
        Notification notification = connectRequestNotification(trainerDetail, memberId, memberName, connectingInfoId);
        notificationRepository.save(notification);
    }

    public void sendDisconnectNotification(PersonalDetail trainerDetail, Long memberId, String memberName) {
        Notification notification = disconnectNotification(trainerDetail, memberId, memberName);
        notificationRepository.save(notification);
    }

    public void sendCancelReservationNotification(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                  Reason reason) {
        Notification notification = cancelReservationNotification(memberDetail, reservationId, trainerId, reason);
        notificationRepository.save(notification);
    }

    public void sendCancelRequestReservationNotification(PersonalDetail trainerDetail,
                                                         Long reservationId, Long memberId, String name,
                                                         LocalDateTime cancelDate, String cancelReason,
                                                         Reason reason) {
        Notification notification = cancelRequestReservationNotification(trainerDetail, reservationId, memberId, name,
                cancelDate, cancelReason, reason);
        notificationRepository.save(notification);
    }

    public void sendApproveReservationNotification(PersonalDetail memberDetail, Long reservationId, Long trainerId) {
        Notification notification = approveReservationNotification(memberDetail, reservationId, trainerId);
        notificationRepository.save(notification);
    }

    public void sendApproveRequestReservationNotification(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                          boolean isApprove) {
        Notification notification = approveRequestReservationNotification(memberDetail, reservationId, trainerId, isApprove);
        notificationRepository.save(notification);
    }

    public void sendRefuseReservationNotification(PersonalDetail memberDetail, Long reservationId, Long trainerId) {
        Notification notification = refuseReservationNotification(memberDetail, reservationId, trainerId);
        notificationRepository.save(notification);
    }

    public void sendRequestReservationNotification(PersonalDetail trainerDetail, Long reservationId, Long memberId, String name) {
        Notification notification = requestReservationNotification(trainerDetail, reservationId, memberId, name);
        notificationRepository.save(notification);
    }

    public void sendCompleteSessionNotification(PersonalDetail memberDetail, Long sessionId, Long trainerId) {
        Notification notification = completeSessionNotification(memberDetail, sessionId, trainerId);
        notificationRepository.save(notification);
    }

    public void sendChangeRequestReservationNotification(PersonalDetail trainerDetail, Long reservationId, Long memberId,
                                                         String name, LocalDateTime reservationDate, LocalDateTime changeDate) {
        Notification notification = changeRequestReservationNotification(trainerDetail, reservationId, memberId, name,
                reservationDate, changeDate);
        notificationRepository.save(notification);
    }

    public void sendCancelApproveReservationNotification(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                         boolean approve) {
        Notification notification = cancelApproveReservationNotification(memberDetail, reservationId, trainerId, approve);
        notificationRepository.save(notification);
    }
}
