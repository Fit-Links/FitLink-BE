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
            String pushToken
    ) implements NotificationRequest {

        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.CONNECT_RESPONSE;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static ConnectDecision of(PersonalDetail memberDetail, Trainer trainer, Boolean isApproved,
                                         String pushToken) {
            return ConnectDecision.builder()
                    .memberDetail(memberDetail)
                    .trainer(trainer)
                    .isApproved(isApproved)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record Connect(
            PersonalDetail trainerDetail, Long memberId, String memberName,
            Long connectingInfoId, String pushToken
    ) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.CONNECT;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }


        public static Connect of(PersonalDetail trainerDetail, Long memberId, String memberName,
                                 Long connectingInfoId, String pushToken) {
            return Connect.builder()
                    .trainerDetail(trainerDetail)
                    .memberId(memberId)
                    .memberName(memberName)
                    .connectingInfoId(connectingInfoId)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record Disconnect(
            PersonalDetail trainerDetail,
            Long memberId,
            String memberName,
            UserRole target,
            String pushToken
    ) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.DISCONNECT;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static Disconnect of(PersonalDetail trainerDetail, Long memberId, String memberName, UserRole target,
                                    String pushToken) {
            return Disconnect.builder()
                    .trainerDetail(trainerDetail)
                    .memberId(memberId)
                    .memberName(memberName)
                    .target(target)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record Cancel(
            PersonalDetail memberDetail,
            Long reservationId,
            Long trainerId,
            Notification.Reason reason,
            String pushToken
    ) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_CANCEL;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static Cancel of(PersonalDetail memberDetail,
                                Long reservationId,
                                Long trainerId,
                                Notification.Reason reason,
                                String pushToken) {
            return Cancel.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .trainerId(trainerId)
                    .reason(reason)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record CancelRequestReservation(
            PersonalDetail trainerDetail,
            Long reservationId, Long memberId, String name,
            LocalDateTime cancelDate, String cancelReason,
            Notification.Reason reason,
            String pushToken) implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_CANCEL_REQUEST;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static CancelRequestReservation of(PersonalDetail trainerDetail,
                                                  Long reservationId, Long memberId, String name,
                                                  LocalDateTime cancelDate, String cancelReason,
                                                  Notification.Reason reason,
                                                  String pushToken) {
            return CancelRequestReservation.builder()
                    .trainerDetail(trainerDetail)
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .name(name)
                    .cancelDate(cancelDate)
                    .cancelReason(cancelReason)
                    .reason(reason)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record ApproveReservation(PersonalDetail memberDetail, Long reservationId, LocalDateTime reservationDate,
                                     Long trainerId, boolean isApprove, String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return isApprove ? Notification.NotificationType.RESERVATION_APPROVE :
                    Notification.NotificationType.RESERVATION_REFUSE;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static ApproveReservation of(PersonalDetail memberDetail, Long reservationId, LocalDateTime reservationDate,
                                            Long trainerId, boolean isApprove, String pushToken) {
            return ApproveReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .reservationDate(reservationDate)
                    .trainerId(trainerId)
                    .isApprove(isApprove)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record ApproveRequestReservation(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                            boolean isApprove, String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return isApprove ? Notification.NotificationType.RESERVATION_CHANGE_REQUEST_APPROVED :
                    Notification.NotificationType.RESERVATION_CHANGE_REQUEST_REFUSED;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static ApproveRequestReservation of(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                   boolean isApprove, String pushToken) {
            return ApproveRequestReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .trainerId(trainerId)
                    .isApprove(isApprove)
                    .pushToken(pushToken)
                    .build();
        }

    }

    @Builder
    public record RequestReservation(PersonalDetail trainerDetail, Long reservationId, LocalDateTime reservationDate,
                                     Long memberId, String name, String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_REQUESTED;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static RequestReservation of(PersonalDetail trainerDetail, Long reservationId, LocalDateTime reservationDate,
                                            Long memberId, String name, String pushToken) {
            return RequestReservation.builder()
                    .trainerDetail(trainerDetail)
                    .reservationId(reservationId)
                    .reservationDate(reservationDate)
                    .memberId(memberId)
                    .name(name)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record CompleteSession(PersonalDetail trainerDetail, Long sessionId, Long memberId, String name,
                                  String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_COMPLETED;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static CompleteSession of(PersonalDetail trainerDetail, Long sessionId, Long memberId, String name,
                                         String pushToken) {
            return CompleteSession.builder()
                    .trainerDetail(trainerDetail)
                    .sessionId(sessionId)
                    .memberId(memberId)
                    .name(name)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record DeductSession(PersonalDetail memberDetail, Long sessionId, Long trainerId,
                                String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_DEDUCTED;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static DeductSession of(PersonalDetail memberDetail, Long sessionId, Long trainerId,
                                       String pushToken) {
            return DeductSession.builder()
                    .memberDetail(memberDetail)
                    .sessionId(sessionId)
                    .trainerId(trainerId)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record ChangeRequestReservation(PersonalDetail trainerDetail, Long reservationId, Long memberId,
                                           String name, LocalDateTime reservationDate, LocalDateTime changeDate,
                                           String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.RESERVATION_CHANGE_REQUEST;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static ChangeRequestReservation of(PersonalDetail trainerDetail, Long reservationId, Long memberId,
                                                  String name, LocalDateTime reservationDate, LocalDateTime changeDate,
                                                  String pushToken) {
            return ChangeRequestReservation.builder()
                    .trainerDetail(trainerDetail)
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .name(name)
                    .reservationDate(reservationDate)
                    .changeDate(changeDate)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record CancelApproveReservation(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                           boolean isApprove, String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return isApprove ? Notification.NotificationType.RESERVATION_CANCEL_REQUEST_APPROVED :
                    Notification.NotificationType.RESERVATION_CANCEL_REQUEST_REFUSED;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static CancelApproveReservation of(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                  boolean isApprove, String pushToken) {
            return CancelApproveReservation.builder()
                    .memberDetail(memberDetail)
                    .reservationId(reservationId)
                    .trainerId(trainerId)
                    .isApprove(isApprove)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record SearchCondition(Notification.ReferenceType type, Pageable pageRequest, String keyword,
                                  Long memberId) {
    }

    @Builder
    public record SessionTodayReminder(PersonalDetail memberDetail, Long sessionId,
                                       Long trainerId, LocalDateTime confirmDate,
                                       String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_REMINDER;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static SessionTodayReminder of(PersonalDetail memberDetail, Long sessionId,
                                              Long trainerId, LocalDateTime confirmDate,
                                              String pushToken) {
            return SessionTodayReminder.builder()
                    .memberDetail(memberDetail)
                    .sessionId(sessionId)
                    .trainerId(trainerId)
                    .confirmDate(confirmDate)
                    .pushToken(pushToken)
                    .build();
        }
    }

    @Builder
    public record EditSession(PersonalDetail memberDetail, Long sessionInfoId, Long trainerId, int beforeTotalCnt,
                              int afterTotalCnt, int beforeRemainingCnt, int afterRemainingCnt,
                              String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_EDITED;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static EditSession of(PersonalDetail memberDetail, Long sessionInfoId, Long trainerId, int beforeTotalCnt,
                                     int afterTotalCnt, int beforeRemainingCnt, int afterRemainingCnt,
                                     String pushToken) {
            return EditSession.builder()
                    .memberDetail(memberDetail)
                    .sessionInfoId(sessionInfoId)
                    .trainerId(trainerId)
                    .beforeTotalCnt(beforeTotalCnt)
                    .afterTotalCnt(afterTotalCnt)
                    .beforeRemainingCnt(beforeRemainingCnt)
                    .afterRemainingCnt(afterRemainingCnt)
                    .pushToken(pushToken)
                    .build();
        }

    }

    @Builder
    public record SessionChargeReminder(PersonalDetail memberDetail, Long sessionInfoId, Long trainerId,
                                        String pushToken)
            implements NotificationRequest {
        @Override
        public Notification.NotificationType getType() {
            return Notification.NotificationType.SESSION_REMAIN_5;
        }

        @Override
        public String getPushToken() {
            return this.pushToken;
        }

        public static SessionChargeReminder of(PersonalDetail memberDetail, Long sessionInfoId, Long trainerId,
                                               String pushToken) {
            return SessionChargeReminder.builder()
                    .memberDetail(memberDetail)
                    .sessionInfoId(sessionInfoId)
                    .trainerId(trainerId)
                    .pushToken(pushToken)
                    .build();
        }

    }
}
