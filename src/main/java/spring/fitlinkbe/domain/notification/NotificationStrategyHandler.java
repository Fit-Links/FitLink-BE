package spring.fitlinkbe.domain.notification;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.notification.command.NotificationCommand;
import spring.fitlinkbe.domain.notification.command.NotificationRequest;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class NotificationStrategyHandler {

    private final Map<Notification.NotificationType, Function<NotificationRequest, Notification>> strategyMap =
            new EnumMap<>(Notification.NotificationType.class);

    @PostConstruct
    public void init() {
        strategyMap.put(Notification.NotificationType.CONNECT, this::handleConnectRequest);
        strategyMap.put(Notification.NotificationType.DISCONNECT, this::handleDisconnect);
        strategyMap.put(Notification.NotificationType.RESERVATION_CANCEL, this::handleCancelReservation);
        strategyMap.put(Notification.NotificationType.RESERVATION_CANCEL_REQUEST, this::handleCancelRequestReservation);
        strategyMap.put(Notification.NotificationType.RESERVATION_APPROVE, this::handleApproveReservation);
        strategyMap.put(Notification.NotificationType.RESERVATION_REFUSE, this::handleApproveReservation);
        strategyMap.put(Notification.NotificationType.RESERVATION_CHANGE_REQUEST_APPROVED, this::handleApproveRequestReservation);
        strategyMap.put(Notification.NotificationType.RESERVATION_CHANGE_REQUEST_REFUSED, this::handleApproveRequestReservation);
        strategyMap.put(Notification.NotificationType.RESERVATION_REQUESTED, this::handleRequestReservation);
        strategyMap.put(Notification.NotificationType.SESSION_COMPLETED, this::handleCompleteSession);
        strategyMap.put(Notification.NotificationType.SESSION_DEDUCTED, this::handleDeductSession);
        strategyMap.put(Notification.NotificationType.RESERVATION_CHANGE_REQUEST, this::handleChangeRequestReservation);
        strategyMap.put(Notification.NotificationType.RESERVATION_CANCEL_REQUEST_APPROVED, this::handleCancelApproveReservation);
        strategyMap.put(Notification.NotificationType.RESERVATION_CANCEL_REQUEST_REFUSED, this::handleCancelApproveReservation);


//        strategyMap.put(Notification.NotificationType.SESSION_REMINDER, this::handleCancelRequestReservation);
//        strategyMap.put(Notification.NotificationType.SESSION_REMAIN_5, this::handleCancelRequestReservation);
//        strategyMap.put(Notification.NotificationType.SESSION_EDITED, this::handleCancelRequestReservation);
    }

    @SuppressWarnings("unchecked")
    public <T extends NotificationRequest> Notification handle(T request) {
        Function<T, Notification> strategy = (Function<T, Notification>) strategyMap.get(request.getType());

        if (strategy == null) {
            throw new CustomException(ErrorCode.NOTIFICATION_STRANGE_TYPE);
        }

        return strategy.apply(request);
    }

    private Notification handleConnectRequest(NotificationRequest request) {
        NotificationCommand.Connect dto = (NotificationCommand.Connect) request;
        return Notification.connectRequest(dto.trainerDetail(), dto.memberId(), dto.memberName(),
                dto.connectingInfoId());
    }

    private Notification handleDisconnect(NotificationRequest request) {
        NotificationCommand.Disconnect dto = (NotificationCommand.Disconnect) request;
        return Notification.disconnect(dto.trainerDetail(), dto.memberId(), dto.memberName(), dto.target());
    }

    private Notification handleCancelReservation(NotificationRequest request) {
        NotificationCommand.CancelReservation dto = (NotificationCommand.CancelReservation) request;
        return Notification.cancelReservation(dto.memberDetail(), dto.reservationId(), dto.trainerId(), dto.reason());
    }

    private Notification handleCancelRequestReservation(NotificationRequest request) {
        NotificationCommand.CancelRequestReservation dto = (NotificationCommand.CancelRequestReservation) request;
        return Notification.cancelRequestReservation(dto.trainerDetail(), dto.reservationId(), dto.memberId(),
                dto.name(), dto.cancelDate(), dto.cancelReason(), dto.reason());
    }

    private Notification handleApproveReservation(NotificationRequest request) {
        NotificationCommand.ApproveReservation dto = (NotificationCommand.ApproveReservation) request;
        return Notification.approveReservation(dto.memberDetail(), dto.reservationId(), dto.reservationDate(),
                dto.trainerId(), dto.isApprove());
    }

    private Notification handleApproveRequestReservation(NotificationRequest request) {
        NotificationCommand.ApproveRequestReservation dto = (NotificationCommand.ApproveRequestReservation) request;
        return Notification.approveRequestReservation(dto.memberDetail(), dto.reservationId(), dto.trainerId(),
                dto.isApprove());
    }

    private Notification handleRequestReservation(NotificationRequest request) {
        NotificationCommand.RequestReservation dto = (NotificationCommand.RequestReservation) request;
        return Notification.requestReservation(dto.trainerDetail(), dto.reservationId(), dto.reservationDate(),
                dto.memberId(), dto.name());
    }

    private Notification handleCompleteSession(NotificationRequest request) {
        NotificationCommand.CompleteSession dto = (NotificationCommand.CompleteSession) request;
        return Notification.completeSession(dto.trainerDetail(), dto.sessionId(), dto.memberId(), dto.name());
    }

    private Notification handleDeductSession(NotificationRequest request) {
        NotificationCommand.DeductSession dto = (NotificationCommand.DeductSession) request;
        return Notification.deductSession(dto.memberDetail(), dto.sessionId(), dto.trainerId());
    }

    private Notification handleChangeRequestReservation(NotificationRequest request) {
        NotificationCommand.ChangeRequestReservation dto = (NotificationCommand.ChangeRequestReservation) request;
        return Notification.changeRequestReservation(dto.trainerDetail(), dto.reservationId(), dto.memberId(),
                dto.name(), dto.reservationDate(), dto.changeDate());
    }

    private Notification handleCancelApproveReservation(NotificationRequest request) {
        NotificationCommand.CancelApproveReservation dto = (NotificationCommand.CancelApproveReservation) request;
        return Notification.cancelApproveReservation(dto.memberDetail(), dto.reservationId(), dto.trainerId(),
                dto.isApprove());
    }
}
