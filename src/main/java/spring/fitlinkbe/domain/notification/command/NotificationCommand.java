package spring.fitlinkbe.domain.notification.command;

import lombok.Builder;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.notification.Notification;

import java.time.LocalDateTime;

public class NotificationCommand {

    @Builder
    public record Connect(
            PersonalDetail trainerDetail, Long memberId, String memberName,
            Long connectingInfoId
    ) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.CONNECT;
        }

        public static Connect of(PersonalDetail trainerDetail, Long memberId, String memberName,
                                 Long connectingInfoId) {
            return Connect.builder()
                    .trainerDetail(trainerDetail)
                    .memberId(memberId)
                    .memberName(memberName)
                    .connectingInfoId(connectingInfoId)
                    .build();
        }
    }

    @Builder
    public record Disconnect(
            PersonalDetail trainerDetail,
            Long memberId,
            String memberName
    ) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.DISCONNECT;
        }

        public static Disconnect of(PersonalDetail trainerDetail, Long memberId, String memberName) {
            return Disconnect.builder()
                    .trainerDetail(trainerDetail)
                    .memberId(memberId)
                    .memberName(memberName)
                    .build();
        }
    }

    @Builder
    public record CancelReservation(
            PersonalDetail memberDetail,
            Long reservationId,
            Long trainerId,
            Notification.Reason reason
    ) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_CANCEL;
        }

        public static CancelReservation of(PersonalDetail memberDetail,
                                           Long reservationId,
                                           Long trainerId,
                                           Notification.Reason reason) {
            return CancelReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .trainerId(trainerId)
                    .reason(reason)
                    .build();
        }
    }

    @Builder
    public record CancelRequestReservation(
            PersonalDetail trainerDetail,
            Long reservationId, Long memberId, String name,
            LocalDateTime cancelDate, String cancelReason,
            Notification.Reason reason) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_CANCEL_REQUEST;
        }

        public static CancelRequestReservation of(PersonalDetail trainerDetail,
                                                  Long reservationId, Long memberId, String name,
                                                  LocalDateTime cancelDate, String cancelReason,
                                                  Notification.Reason reason) {
            return CancelRequestReservation.builder()
                    .trainerDetail(trainerDetail)
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .name(name)
                    .cancelDate(cancelDate)
                    .cancelReason(cancelReason)
                    .reason(reason)
                    .build();
        }
    }

    @Builder
    public record ApproveReservation(PersonalDetail memberDetail, Long reservationId, LocalDateTime reservationDate,
                                     Long trainerId, boolean isApprove)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return isApprove ? Notification.NotificationType.RESERVATION_APPROVE :
                    Notification.NotificationType.RESERVATION_REFUSE;
        }

        public static ApproveReservation of(PersonalDetail memberDetail, Long reservationId, LocalDateTime reservationDate,
                                            Long trainerId,
                                            boolean isApprove) {
            return ApproveReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .reservationDate(reservationDate)
                    .trainerId(trainerId)
                    .isApprove(isApprove)
                    .build();
        }
    }

    @Builder
    public record ApproveRequestReservation(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                            boolean isApprove)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return isApprove ? Notification.NotificationType.RESERVATION_CHANGE_REQUEST_APPROVED :
                    Notification.NotificationType.RESERVATION_CHANGE_REQUEST_REFUSED;
        }

        public static ApproveRequestReservation of(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                   boolean isApprove) {
            return ApproveRequestReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .trainerId(trainerId)
                    .isApprove(isApprove)
                    .build();
        }

    }

    @Builder
    public record RequestReservation(PersonalDetail trainerDetail, Long reservationId, Long memberId, String name)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_REQUESTED;
        }

        public static RequestReservation of(PersonalDetail trainerDetail, Long reservationId, Long memberId, String name) {
            return RequestReservation.builder()
                    .trainerDetail(trainerDetail)
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .name(name)
                    .build();
        }
    }

    @Builder
    public record CompleteSession(PersonalDetail trainerDetail, Long sessionId, Long memberId, String name)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_COMPLETED;
        }

        public static CompleteSession of(PersonalDetail trainerDetail, Long sessionId, Long memberId, String name) {
            return CompleteSession.builder()
                    .trainerDetail(trainerDetail)
                    .sessionId(sessionId)
                    .memberId(memberId)
                    .name(name)
                    .build();
        }
    }

    @Builder
    public record DeductSession(PersonalDetail memberDetail, Long sessionId, Long trainerId)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_DEDUCTED;
        }

        public static DeductSession of(PersonalDetail memberDetail, Long sessionId, Long trainerId) {
            return DeductSession.builder()
                    .memberDetail(memberDetail)
                    .sessionId(sessionId)
                    .trainerId(trainerId)
                    .build();
        }
    }

    @Builder
    public record ChangeRequestReservation(PersonalDetail trainerDetail, Long reservationId, Long memberId,
                                           String name, LocalDateTime reservationDate, LocalDateTime changeDate)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_CHANGE_REQUEST;
        }

        public static ChangeRequestReservation of(PersonalDetail trainerDetail, Long reservationId, Long memberId,
                                                  String name, LocalDateTime reservationDate, LocalDateTime changeDate) {
            return ChangeRequestReservation.builder()
                    .trainerDetail(trainerDetail)
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .name(name)
                    .reservationDate(reservationDate)
                    .changeDate(changeDate)
                    .build();
        }
    }

    @Builder
    public record CancelApproveReservation(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                           boolean isApprove)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return isApprove ? Notification.NotificationType.RESERVATION_CANCEL_REQUEST_APPROVED :
                    Notification.NotificationType.RESERVATION_CANCEL_REQUEST_REFUSED;
        }

        public static CancelApproveReservation of(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                  boolean isApprove) {
            return CancelApproveReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .trainerId(trainerId)
                    .isApprove(isApprove)
                    .build();
        }
    }

    @Builder
    public record GetNotifications(Notification.ReferenceType type, Pageable pageRequest) {
    }
}
