package spring.fitlinkbe.domain.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.reservation.Reservation;

import java.time.LocalDateTime;

import static spring.fitlinkbe.domain.notification.Notification.*;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void sendConnectRequestNotification(PersonalDetail trainerDetail, String memberName, Long connectingInfoId) {
        Notification notification = connectRequestNotification(trainerDetail, memberName, connectingInfoId);
        notificationRepository.save(notification);
    }

    public void sendDisconnectNotification(String name, PersonalDetail trainerDetail) {
        Notification notification = disconnectNotification(name, trainerDetail);
        notificationRepository.save(notification);
    }

    public void sendCancelReservationNotification(Long reservationId, PersonalDetail memberDetail, Reason reason) {
        Notification notification = cancelReservationNotification(reservationId, memberDetail, reason);
        notificationRepository.save(notification);
    }

    public void sendCancelRequestReservationNotification(Long reservationId, String name,
                                                         LocalDateTime cancelDate, String cancelReason,
                                                         PersonalDetail trainerDetail,
                                                         Reason reason) {
        Notification notification = cancelRequestReservationNotification(reservationId, name,
                cancelDate, cancelReason, trainerDetail, reason);
        notificationRepository.save(notification);
    }

    public void sendApproveReservationNotification(Long reservationId, PersonalDetail memberDetail) {
        Notification notification = approveReservationNotification(reservationId, memberDetail);
        notificationRepository.save(notification);
    }

    public void sendApproveRequestReservationNotification(Long reservationId, PersonalDetail memberDetail,
                                                          boolean isApprove) {
        Notification notification = approveRequestReservationNotification(reservationId, memberDetail, isApprove);
        notificationRepository.save(notification);
    }

    public void sendRefuseReservationNotification(Long reservationId, PersonalDetail trainerDetail) {
        Notification notification = refuseReservationNotification(reservationId, trainerDetail);
        notificationRepository.save(notification);
    }

    public void sendRequestReservationNotification(Reservation reservation, PersonalDetail trainerDetail) {
        Notification notification = requestReservationNotification(reservation, trainerDetail);
        notificationRepository.save(notification);
    }

    public void sendCompleteSessionNotification(Long sessionId, PersonalDetail memberDetail) {
        Notification notification = completeSessionNotification(sessionId, memberDetail);
        notificationRepository.save(notification);
    }

    public void sendChangeRequestReservationNotification(Long reservationId, String name, LocalDateTime reservationDate,
                                                         LocalDateTime changeDate,
                                                         PersonalDetail memberDetail) {
        Notification notification = changeRequestReservationNotification(reservationId, name,
                reservationDate, changeDate, memberDetail);
        notificationRepository.save(notification);
    }
}
