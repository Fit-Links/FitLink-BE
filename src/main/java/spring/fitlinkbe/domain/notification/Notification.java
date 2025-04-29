package spring.fitlinkbe.domain.notification;

import lombok.*;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

import java.time.LocalDateTime;

import static spring.fitlinkbe.support.utils.DateUtils.formatDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long notificationId;
    private Long refId;
    private ReferenceType refType;
    private UserRole target;
    private Long partnerId;
    private NotificationType notificationType;
    private PersonalDetail personalDetail;
    private String name;
    private String content;
    private boolean isSent;
    private boolean isProcessed;
    private LocalDateTime sendDate;

    public static Notification connectRequest(PersonalDetail trainerDetail,
                                              Long memberId, String memberName, Long connectingInfoId) {
        String content = memberName + " 님에게 연동 요청이 왔습니다.";

        return Notification.builder()
                .refId(connectingInfoId)
                .refType(ReferenceType.CONNECT)
                .target(UserRole.TRAINER)
                .notificationType(NotificationType.CONNECT)
                .personalDetail(trainerDetail)
                .partnerId(memberId)
                .name(NotificationType.CONNECT.getName())
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification disconnect(PersonalDetail userDetail, Long partnerId, String userName,
                                          UserRole target) {
        String content = userName + " 님과의 연동 (또는 연동 요청) 이 취소되었습니다.";

        return Notification.builder()
                .refId(null)
                .refType(ReferenceType.DISCONNECT)
                .target(target)
                .notificationType(target == UserRole.TRAINER ? NotificationType.DISCONNECT :
                        NotificationType.DISCONNECT_TRAINER)
                .personalDetail(userDetail)
                .partnerId(partnerId)
                .name(NotificationType.DISCONNECT.getName())
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification cancelReservation(PersonalDetail memberDetail, Long reservationId, Long trainerId,
                                                 Reason reason) {

        String content = "트레이너님의 %s로 인해 %s 회원님의 예약이 취소되었습니다.".formatted(
                reason.name,
                memberDetail.getName());

        return Notification.builder()
                .refId(reservationId)
                .refType(ReferenceType.RESERVATION_CANCEL)
                .target(UserRole.MEMBER)
                .notificationType(NotificationType.RESERVATION_CANCEL)
                .personalDetail(memberDetail)
                .partnerId(trainerId)
                .name(NotificationType.RESERVATION_CANCEL.getName())
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification cancelRequestReservation(PersonalDetail trainerDetail, Long reservationId,
                                                        Long memberId, String name, LocalDateTime cancelDate,
                                                        String cancelReason, Reason reason) {
        String content = ("%s 회원님이 %s를 요청하였습니다.\n 날짜: %s\n취소 사유: %s")
                .formatted(name, reason.name, formatDateTime(cancelDate), cancelReason);

        return Notification.builder()
                .refId(reservationId)
                .refType(ReferenceType.RESERVATION_CANCEL)
                .target(UserRole.TRAINER)
                .notificationType(NotificationType.RESERVATION_CANCEL)
                .personalDetail(trainerDetail)
                .partnerId(memberId)
                .name(NotificationType.RESERVATION_CANCEL.getName())
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();

    }

    public static Notification approveReservation(PersonalDetail memberDetail, Long reservationId,
                                                  LocalDateTime reservationDate, Long trainerId, boolean isApprove) {

        String content = "%s 회원님의 예약이 %s되었습니다.\n 날짜: %s".formatted(memberDetail.getName(), isApprove ? "확정" : "거절",
                formatDateTime(reservationDate));
        return Notification.builder()
                .refId(reservationId)
                .refType(ReferenceType.RESERVATION_REQUEST)
                .target(UserRole.MEMBER)
                .notificationType(isApprove ? NotificationType.RESERVATION_APPROVE : NotificationType.RESERVATION_REFUSE)
                .personalDetail(memberDetail)
                .partnerId(trainerId)
                .name(isApprove ? NotificationType.RESERVATION_APPROVE.name : NotificationType.RESERVATION_REFUSE.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification approveRequestReservation(PersonalDetail memberDetail, Long reservationId,
                                                         Long trainerId, boolean isApprove) {

        String content = "%s 회원님의 예약 변경이 %s되었습니다."
                .formatted(memberDetail.getName(), isApprove ? "승인" : "거절");

        return Notification.builder()
                .refId(reservationId)
                .refType(ReferenceType.RESERVATION_CHANGE)
                .target(UserRole.MEMBER)
                .notificationType(isApprove ? NotificationType.RESERVATION_CHANGE_REQUEST_APPROVED :
                        NotificationType.RESERVATION_CHANGE_REQUEST_REFUSED)
                .personalDetail(memberDetail)
                .partnerId(trainerId)
                .name(isApprove ? NotificationType.RESERVATION_CHANGE_REQUEST_APPROVED.name :
                        NotificationType.RESERVATION_CHANGE_REQUEST_REFUSED.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification requestReservation(PersonalDetail trainerDetail, Long reservationId,
                                                  LocalDateTime reservationDate, Long memberId, String name) {

        String content = " %s 회원님이 PT 예약을 요청하였습니다.\n날짜: %s".formatted(name, formatDateTime(reservationDate));

        return Notification.builder()
                .refId(reservationId)
                .refType(ReferenceType.RESERVATION_REQUEST)
                .target(UserRole.TRAINER)
                .notificationType(NotificationType.RESERVATION_REQUESTED)
                .personalDetail(trainerDetail)
                .partnerId(memberId)
                .name(NotificationType.RESERVATION_REQUESTED.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification completeSession(PersonalDetail memberDetail, Long sessionId,
                                               Long memberId, String name) {

        String content = " %s 회원님의 PT가 종료되었습니다.".formatted(name);

        return Notification.builder()
                .refId(sessionId)
                .refType(ReferenceType.SESSION)
                .target(UserRole.TRAINER)
                .notificationType(NotificationType.SESSION_COMPLETED)
                .personalDetail(memberDetail)
                .partnerId(memberId)
                .name(NotificationType.SESSION_COMPLETED.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification deductSession(PersonalDetail memberDetail, Long sessionId,
                                             Long trainerId) {

        String content = " PT 완료로 횟수가 1회 차감되었습니다.";

        return Notification.builder()
                .refId(sessionId)
                .refType(ReferenceType.SESSION)
                .target(UserRole.MEMBER)
                .notificationType(NotificationType.SESSION_DEDUCTED)
                .personalDetail(memberDetail)
                .partnerId(trainerId)
                .name(NotificationType.SESSION_DEDUCTED.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification changeRequestReservation(PersonalDetail trainerDetail, Long reservationId,
                                                        Long memberId, String name, LocalDateTime reservationDate,
                                                        LocalDateTime changeDate) {

        String content = ("%s 회원님의 PT 예약 변경이 요청되었습니다. \n " +
                "날짜: %s -> %s").formatted(name, formatDateTime(reservationDate),
                formatDateTime(changeDate));

        return Notification.builder()
                .refId(reservationId)
                .refType(ReferenceType.RESERVATION_CHANGE)
                .target(UserRole.TRAINER)
                .notificationType(NotificationType.RESERVATION_CHANGE_REQUEST)
                .personalDetail(trainerDetail)
                .partnerId(memberId)
                .name(NotificationType.RESERVATION_CHANGE_REQUEST.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification cancelApproveReservation(PersonalDetail memberDetail, Long reservationId,
                                                        Long trainerId, boolean isApprove) {

        String content = "%s 회원님의 예약 취소 요청이 %s되었습니다."
                .formatted(memberDetail.getName(), isApprove ? "승인" : "거절");


        return Notification.builder()
                .refId(reservationId)
                .refType(ReferenceType.RESERVATION_CANCEL)
                .target(UserRole.MEMBER)
                .notificationType(isApprove ? NotificationType.RESERVATION_CANCEL_REQUEST_APPROVED :
                        NotificationType.RESERVATION_CANCEL_REQUEST_REFUSED)
                .personalDetail(memberDetail)
                .partnerId(trainerId)
                .name(isApprove ? NotificationType.RESERVATION_CHANGE_REQUEST.name :
                        NotificationType.RESERVATION_CANCEL_REQUEST_REFUSED.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification sessionTodayReminder(PersonalDetail memberDetail, Long sessionId,
                                                    Long trainerId, LocalDateTime confirmDate) {

        String content = "오늘 %s에 PT가 진행됩니다.".formatted(formatDateTime(confirmDate));


        return Notification.builder()
                .refId(sessionId)
                .refType(ReferenceType.SESSION)
                .target(UserRole.MEMBER)
                .notificationType(NotificationType.SESSION_REMINDER)
                .personalDetail(memberDetail)
                .partnerId(trainerId)
                .name(NotificationType.SESSION_REMINDER.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification sessionChargeReminder(PersonalDetail memberDetail, Long sessionInfoId,
                                                     Long trainerId) {

        String content = "PT 횟수가 얼마 남지 않았습니다. \n 남은 잔여 횟수: 5회";


        return Notification.builder()
                .refId(sessionInfoId)
                .refType(ReferenceType.SESSION)
                .target(UserRole.MEMBER)
                .notificationType(NotificationType.SESSION_REMAIN_5)
                .personalDetail(memberDetail)
                .partnerId(trainerId)
                .name(NotificationType.SESSION_REMAIN_5.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification editSession(PersonalDetail memberDetail, Long sessionInfoId, Long trainerId,
                                           int beforeTotalCnt, int afterTotalCnt, int beforeRemainingCnt,
                                           int afterRemainingCnt) {

        String content = "트레이너가 회원님의 세션 정보를 수정하였습니다.\n 총 세션: %s -> %s \n 남은 세션 %s -> %s".formatted(
                beforeTotalCnt, afterTotalCnt, beforeRemainingCnt, afterRemainingCnt);

        return Notification.builder()
                .refId(sessionInfoId)
                .refType(ReferenceType.SESSION)
                .target(UserRole.MEMBER)
                .notificationType(NotificationType.SESSION_EDITED)
                .personalDetail(memberDetail)
                .partnerId(trainerId)
                .name(NotificationType.SESSION_EDITED.name)
                .content(content)
                .sendDate(LocalDateTime.now())
                .build();
    }

    @RequiredArgsConstructor
    @Getter
    public enum ReferenceType {
        CONNECT("트레이너 연동"),
        DISCONNECT("트레이너 연동 해제"),
        RESERVATION_REQUEST("예약 요청"),
        RESERVATION_CHANGE("예약 변경"),
        RESERVATION_CANCEL("예약 취소"),
        SESSION("세션");

        private final String name;
    }

    @RequiredArgsConstructor
    @Getter
    public enum NotificationType {
        //트레이너
        RESERVATION_REQUESTED("예약 요청", "예약이 요청되었습니다."),
        RESERVATION_CANCEL_REQUEST("예약 취소 요청", "예약 취소가 요청되었습니다"),
        RESERVATION_CHANGE_REQUEST("예약 변경 요청", "예약 변경이 요청되었습니다"),
        SESSION_COMPLETED("세션 완료", "세션이 완료 되었습니다."),
        CONNECT("트레이너 연동 요청", "트레이너와 연동 요청이 왔습니다."),
        DISCONNECT("트레이너 연동 해제", "회원과 연동이 해제되었습니다."),

        //회원
        RESERVATION_CHANGE_REQUEST_APPROVED("예약 변경 요청 승인", "예약 변경 요청이 승인 되었습니다"),
        RESERVATION_CHANGE_REQUEST_REFUSED("예약 변경 요청 거절", "예약 변경 요청이 거절 되었습니다"),
        RESERVATION_APPROVE("예약 확정", "예약이 확정되었습니다."),
        RESERVATION_CANCEL("예약 취소", "예약이 취소되었습니다."),
        RESERVATION_REFUSE("예약 거절", "예약이 거절되었습니다."),
        SESSION_DEDUCTED("세션 차감", "세션이 1회 차감되었습니다"),
        SESSION_REMINDER("금일 세션 진행", "오늘 세션이 진행됩니다."),
        RESERVATION_CANCEL_REQUEST_APPROVED("예약 취소 요청 승인", "예약 취소 요청이 승인 되었습니다."),
        RESERVATION_CANCEL_REQUEST_REFUSED("예약 취소 요청 거절", "예약 취소 요청이 거절 되었습니다."),
        SESSION_REMAIN_5("세션 5회 남음", "세션이 5회 남았습니다."),
        SESSION_EDITED("세션 직접 수정", "트레이너가 세션을 수정하였습니다"),
        DISCONNECT_TRAINER("트레이너 연동 해제", "트레이너와 연동이 해제되었습니다."),
        ;


        private final String name;
        private final String description;
    }

    @RequiredArgsConstructor
    public enum Reason {
        DAY_OFF("연차"),
        RESERVATION_REFUSE("예약 거절"),
        RESERVATION_CANCEL("예약 취소");
        private final String name;
    }
}
