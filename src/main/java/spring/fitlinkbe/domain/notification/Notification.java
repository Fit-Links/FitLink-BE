package spring.fitlinkbe.domain.notification;

import lombok.*;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.reservation.Reservation;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long notificationId;
    private Long refId;
    private ReferenceType refType;
    private NotificationType notificationType;
    private PersonalDetail personalDetail;
    private String name;
    private String content;
    private Boolean isSent;
    private Boolean isProcessed;
    private LocalDateTime sendDate;

    public static Notification connectRequestNotification(PersonalDetail trainerDetail,
                                                          String memberName, Long connectingInfoId) {
        String content = memberName + " 님에게 연동 요청이 왔습니다.";

        return Notification.builder()
                .refId(connectingInfoId)
                .refType(ReferenceType.CONNECTING)
                .notificationType(NotificationType.CONNECT)
                .personalDetail(trainerDetail)
                .name(NotificationType.CONNECT.getName())
                .content(content)
                .isSent(true)
                .isProcessed(false)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification disconnectNotification(String memberName, PersonalDetail trainerDetail) {
        String content = memberName + " 님과의 연동이 해제되었습니다.";

        return Notification.builder()
                .refId(null)
                .refType(ReferenceType.CONNECTING)
                .notificationType(NotificationType.DISCONNECT)
                .personalDetail(trainerDetail)
                .name(NotificationType.DISCONNECT.getName())
                .content(content)
                .isSent(true)
                .isProcessed(false)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification cancelReservationNotification(Long reservationId, PersonalDetail memberDetail,
                                                             Reason reason) {

        String content = "트레이너님의 %s로 인해 %s 님의 예약이 취소되었습니다.".formatted(
                reason.name,
                memberDetail.getName());

        return Notification.builder()
                .refId(reservationId)
                .refType(ReferenceType.RESERVATION)
                .notificationType(NotificationType.RESERVATION_CANCEL)
                .personalDetail(memberDetail)
                .name(NotificationType.RESERVATION_CANCEL.getName())
                .content(content)
                .isSent(true)
                .isProcessed(false)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification approveReservationNotification(Long reservationId, PersonalDetail memberDetail) {

        String content = " %s 님의 예약이 승인되었습니다.".formatted(memberDetail.getName());

        return Notification.builder()
                .refId(reservationId)
                .refType(ReferenceType.RESERVATION)
                .notificationType(NotificationType.RESERVATION_APPROVE)
                .personalDetail(memberDetail)
                .name(NotificationType.RESERVATION_APPROVE.name)
                .content(content)
                .isSent(true)
                .isProcessed(false)
                .sendDate(LocalDateTime.now())
                .build();
    }

    public static Notification requestReservationNotification(Reservation reservation, PersonalDetail trainerDetail) {

        String content = " %s 회원님이 PT 예약을 요청하였습니다.".formatted(reservation.getName());

        return Notification.builder()
                .refId(reservation.getReservationId())
                .refType(ReferenceType.RESERVATION)
                .notificationType(NotificationType.RESERVATION_REQUESTED)
                .personalDetail(trainerDetail)
                .name(NotificationType.RESERVATION_REQUESTED.name)
                .content(content)
                .isSent(true)
                .isProcessed(false)
                .sendDate(LocalDateTime.now())
                .build();
    }

    @RequiredArgsConstructor
    @Getter
    public enum ReferenceType {
        RESERVATION("예약"),
        SESSION("세션"),
        CONNECTING("트레이너 연동");

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
        RESERVATION_CHANGE_REQUEST_REFUSED("세션 변경 요청 거절", "세션 변경 요청이 거절 되었습니다"),
        RESERVATION_APPROVE("예약 승인", "예약이 승인되었습니다."),
        RESERVATION_CANCEL("예약 취소", "예약이 취소되었습니다."),
        SESSION_DEDUCTED("세션 차감", "세션이 1회 차감되었습니다"),
        SESSION_REMINDER("금일 세션 진행", "오늘 세션이 진행됩니다."),
        SESSION_CANCEL_REQUEST_APPROVED("세션 취소 요청 승인", "세션 취소 요청이 승인 되었습니다."),
        SESSION_CANCEL_REQUEST_REFUSED("세션 취소 요청 거절", "세션 취소 요청이 거절 되었습니다."),
        SESSION_REMAIN_5("세션 5회 남음", "세션이 5회 남았습니다."),
        SESSION_EDITED("세션 직접 수정", "트레이너가 세션을 수정하였습니다");


        private final String name;
        private final String description;
    }

    @RequiredArgsConstructor
    public enum Reason {
        DAY_OFF("연차"),
        RESERVATION_REFUSE("예약 거절");
        private final String name;
    }
}
