package spring.fitlinkbe.domain.notification.command;

import lombok.Builder;
import org.springframework.data.domain.Pageable;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.LocalDateTime;

public class NotificationCommand {


    @Builder
    public record ConnectDecision(
            PersonalDetail memberDetail,
            Trainer trainer,
            Boolean isApproved,
            String fcmToken
    ) implements NotificationRequest {

        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.CONNECT_RESPONSE;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static ConnectDecision of(PersonalDetail memberDetail, Trainer trainer, Boolean isApproved,
                                         String fcmToken) {
            return ConnectDecision.builder()
                    .memberDetail(memberDetail)
                    .trainer(trainer)
                    .isApproved(isApproved)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record Connect(
            PersonalDetail trainerDetail, Long memberId, String memberName,
            Long connectingInfoId, String fcmToken
    ) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.CONNECT;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }


        public static Connect of(PersonalDetail trainerDetail, Long memberId, String memberName,
                                 Long connectingInfoId, String fcmToken) {
            return Connect.builder()
                    .trainerDetail(trainerDetail)
                    .memberId(memberId)
                    .memberName(memberName)
                    .connectingInfoId(connectingInfoId)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record Disconnect(
            PersonalDetail trainerDetail,
            Long memberId,
            String memberName,
            UserRole target,
            String fcmToken
    ) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.DISCONNECT;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static Disconnect of(PersonalDetail trainerDetail, Long memberId, String memberName, UserRole target,
                                    String fcmToken) {
            return Disconnect.builder()
                    .trainerDetail(trainerDetail)
                    .memberId(memberId)
                    .memberName(memberName)
                    .target(target)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record Cancel(
            PersonalDetail memberDetail,
            Long reservationId,
            Long trainerId,
            Notification.Reason reason,
            String fcmToken
    ) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_CANCEL;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static Cancel of(PersonalDetail memberDetail,
                                Long reservationId,
                                Long trainerId,
                                Notification.Reason reason,
                                String fcmToken) {
            return Cancel.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .trainerId(trainerId)
                    .reason(reason)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record CancelRequestReservation(
            PersonalDetail trainerDetail,
            Long reservationId, Long memberId, String name,
            LocalDateTime cancelDate, String cancelReason,
            Notification.Reason reason,
            String fcmToken) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_CANCEL_REQUEST;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static CancelRequestReservation of(PersonalDetail trainerDetail,
                                                  Long reservationId, Long memberId, String name,
                                                  LocalDateTime cancelDate, String cancelReason,
                                                  Notification.Reason reason,
                                                  String fcmToken) {
            return CancelRequestReservation.builder()
                    .trainerDetail(trainerDetail)
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .name(name)
                    .cancelDate(cancelDate)
                    .cancelReason(cancelReason)
                    .reason(reason)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record ApproveReservation(PersonalDetail memberDetail, Long reservationId, LocalDateTime reservationDate,
                                     Long trainerId, boolean isApprove, String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return isApprove ? Notification.NotificationType.RESERVATION_APPROVE :
                    Notification.NotificationType.RESERVATION_REFUSE;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static ApproveReservation of(PersonalDetail memberDetail, Long reservationId, LocalDateTime reservationDate,
                                            Long trainerId, boolean isApprove, String fcmToken) {
            return ApproveReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .reservationDate(reservationDate)
                    .trainerId(trainerId)
                    .isApprove(isApprove)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record ApproveRequestReservation(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                            boolean isApprove, String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return isApprove ? Notification.NotificationType.RESERVATION_CHANGE_REQUEST_APPROVED :
                    Notification.NotificationType.RESERVATION_CHANGE_REQUEST_REFUSED;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static ApproveRequestReservation of(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                   boolean isApprove, String fcmToken) {
            return ApproveRequestReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .trainerId(trainerId)
                    .isApprove(isApprove)
                    .fcmToken(fcmToken)
                    .build();
        }

    }

    @Builder
    public record RequestReservation(PersonalDetail trainerDetail, Long reservationId, LocalDateTime reservationDate,
                                     Long memberId, String name, String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_REQUESTED;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static RequestReservation of(PersonalDetail trainerDetail, Long reservationId, LocalDateTime reservationDate,
                                            Long memberId, String name, String fcmToken) {
            return RequestReservation.builder()
                    .trainerDetail(trainerDetail)
                    .reservationId(reservationId)
                    .reservationDate(reservationDate)
                    .memberId(memberId)
                    .name(name)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record CompleteSession(PersonalDetail trainerDetail, Long sessionId, Long memberId, String name,
                                  String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_COMPLETED;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static CompleteSession of(PersonalDetail trainerDetail, Long sessionId, Long memberId, String name,
                                         String fcmToken) {
            return CompleteSession.builder()
                    .trainerDetail(trainerDetail)
                    .sessionId(sessionId)
                    .memberId(memberId)
                    .name(name)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record DeductSession(PersonalDetail memberDetail, Long sessionId, Long trainerId,
                                String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_DEDUCTED;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static DeductSession of(PersonalDetail memberDetail, Long sessionId, Long trainerId,
                                       String fcmToken) {
            return DeductSession.builder()
                    .memberDetail(memberDetail)
                    .sessionId(sessionId)
                    .trainerId(trainerId)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record ChangeRequestReservation(PersonalDetail trainerDetail, Long reservationId, Long memberId,
                                           String name, LocalDateTime reservationDate, LocalDateTime changeDate,
                                           String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_CHANGE_REQUEST;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static ChangeRequestReservation of(PersonalDetail trainerDetail, Long reservationId, Long memberId,
                                                  String name, LocalDateTime reservationDate, LocalDateTime changeDate,
                                                  String fcmToken) {
            return ChangeRequestReservation.builder()
                    .trainerDetail(trainerDetail)
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .name(name)
                    .reservationDate(reservationDate)
                    .changeDate(changeDate)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record CancelApproveReservation(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                           boolean isApprove, String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return isApprove ? Notification.NotificationType.RESERVATION_CANCEL_REQUEST_APPROVED :
                    Notification.NotificationType.RESERVATION_CANCEL_REQUEST_REFUSED;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static CancelApproveReservation of(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                  boolean isApprove, String fcmToken) {
            return CancelApproveReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .trainerId(trainerId)
                    .isApprove(isApprove)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record GetNotifications(Notification.ReferenceType type, Pageable pageRequest, String keyword) {
    }

    @Builder
    public record SessionTodayReminder(PersonalDetail memberDetail, Long sessionId,
                                       Long trainerId, LocalDateTime confirmDate,
                                       String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_REMINDER;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static SessionTodayReminder of(PersonalDetail memberDetail, Long sessionId,
                                              Long trainerId, LocalDateTime confirmDate,
                                              String fcmToken) {
            return SessionTodayReminder.builder()
                    .memberDetail(memberDetail)
                    .sessionId(sessionId)
                    .trainerId(trainerId)
                    .confirmDate(confirmDate)
                    .fcmToken(fcmToken)
                    .build();
        }
    }

    @Builder
    public record EditSession(PersonalDetail memberDetail, Long sessionInfoId, Long trainerId, int beforeTotalCnt,
                              int afterTotalCnt, int beforeRemainingCnt, int afterRemainingCnt,
                              String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_EDITED;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static EditSession of(PersonalDetail memberDetail, Long sessionInfoId, Long trainerId, int beforeTotalCnt,
                                     int afterTotalCnt, int beforeRemainingCnt, int afterRemainingCnt,
                                     String fcmToken) {
            return EditSession.builder()
                    .memberDetail(memberDetail)
                    .sessionInfoId(sessionInfoId)
                    .trainerId(trainerId)
                    .beforeTotalCnt(beforeTotalCnt)
                    .afterTotalCnt(afterTotalCnt)
                    .beforeRemainingCnt(beforeRemainingCnt)
                    .afterRemainingCnt(afterRemainingCnt)
                    .fcmToken(fcmToken)
                    .build();
        }

    }

    @Builder
    public record SessionChargeReminder(PersonalDetail memberDetail, Long sessionInfoId, Long trainerId,
                                        String fcmToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_REMAIN_5;
        }

        @Override
        public String getPushToken() {
            return this.fcmToken;
        }

        public static SessionChargeReminder of(PersonalDetail memberDetail, Long sessionInfoId, Long trainerId,
                                               String fcmToken) {
            return SessionChargeReminder.builder()
                    .memberDetail(memberDetail)
                    .sessionInfoId(sessionInfoId)
                    .trainerId(trainerId)
                    .fcmToken(fcmToken)
                    .build();
        }

    }
}
