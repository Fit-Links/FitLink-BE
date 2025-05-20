package spring.fitlinkbe.application.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.application.trainer.criteria.*;
import spring.fitlinkbe.domain.auth.AuthService;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.common.model.Token;
import spring.fitlinkbe.domain.member.MemberService;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.domain.notification.NotificationService;
import spring.fitlinkbe.domain.notification.command.NotificationCommand;
import spring.fitlinkbe.domain.reservation.ReservationService;
import spring.fitlinkbe.domain.trainer.AvailableTime;
import spring.fitlinkbe.domain.trainer.DayOff;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.domain.trainer.TrainerService;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrainerFacade {
    private final TrainerService trainerService;
    private final ReservationService reservationService;
    private final MemberService memberService;
    private final NotificationService notificationService;
    private final AuthService authService;


    public TrainerInfoResult.Response getTrainerInfo(Long trainerId) {
        Trainer trainer = trainerService.getTrainerInfo(trainerId);
        PersonalDetail personalDetail = trainerService.getTrainerDetail(trainerId);

        return TrainerInfoResult.Response.of(trainer, personalDetail);
    }

    @Transactional
    public TrainerInfoResult.Response updateTrainerInfo(
            Long trainerId,
            TrainerInfoResult.TrainerUpdateRequest request
    ) {
        Trainer trainer = trainerService.getTrainerInfo(trainerId);
        PersonalDetail personalDetail = trainerService.getTrainerDetail(trainerId);

        if (request.name() != null) {
            trainer.updateName(request.name());
            personalDetail.updateName(request.name());
        }

        if (request.phoneNumber() != null) {
            personalDetail.updatePhoneNumber(request.phoneNumber());
        }
        trainerService.saveTrainer(trainer);
        trainerService.savePersonalDetail(personalDetail);

        return TrainerInfoResult.TrainerUpdateResponse.of(trainer, personalDetail);
    }

    public TrainerInfoResult.TrainerCodeResponse getTrainerCode(Long trainerId) {
        Trainer trainer = trainerService.getTrainerInfo(trainerId);

        return TrainerInfoResult.TrainerCodeResponse.from(trainer.getTrainerCode());
    }

    public AvailableTimesResult.Response getAvailableTimes(Long trainerId) {
        List<AvailableTime> currentSchedules = trainerService.getCurrentAvailableTimes(trainerId);
        List<AvailableTime> scheduledSchedules = trainerService.getScheduledAvailableTimes(trainerId);

        return AvailableTimesResult.Response.of(currentSchedules, scheduledSchedules);
    }

    public AvailableTimesResult.CurrentAvailableTimesResponse getCurrentAvailableTimes(Long memberId, Long trainerId) {
        memberService.checkConnected(trainerId, memberId);

        List<AvailableTime> currentSchedules = trainerService.getCurrentAvailableTimes(trainerId);
        return AvailableTimesResult.CurrentAvailableTimesResponse.of(currentSchedules);
    }

    @Transactional
    public void saveAvailableTimes(Long trainerId, AvailableTimeCriteria.AddRequest criteria) {
        Trainer trainer = trainerService.getTrainerInfo(trainerId);

        if (criteria.applyAt().equals(LocalDate.now())) {
            saveCurrentApplyAvailableTimes(trainer, criteria);
        } else {
            saveScheduledAvailableTimes(trainer, criteria);
        }
    }

    private void saveCurrentApplyAvailableTimes(Trainer trainer, AvailableTimeCriteria.AddRequest criteria) {
        if (!trainerService.getCurrentAvailableTimes(trainer.getTrainerId()).isEmpty()) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED_AVAILABLE_TIMES);
        }
        List<AvailableTime> availableTimes = criteria.toAvailableTimes(trainer);
        trainerService.saveAvailableTimes(availableTimes);
    }

    private void saveScheduledAvailableTimes(Trainer trainer, AvailableTimeCriteria.AddRequest criteria) {
        if (!trainerService.getScheduledAvailableTimes(trainer.getTrainerId()).isEmpty()) {
            throw new CustomException(ErrorCode.ALREADY_SCHEDULED_AVAILABLE_TIMES);
        }
        List<AvailableTime> availableTimes = criteria.toAvailableTimes(trainer);
        trainerService.saveAvailableTimes(availableTimes);
    }


    public void deleteAvailableTimes(Long trainerId, LocalDate applyAt) {
        List<AvailableTime> availableTimes = trainerService.getAvailableTimes(trainerId, applyAt);
        if (availableTimes.isEmpty()) {
            throw new CustomException(ErrorCode.AVAILABLE_TIMES_IS_NOT_FOUND);
        }

        trainerService.deleteAvailableTimes(availableTimes);
    }

    public List<DayOffResult.Response> saveDayOff(Long trainerId, List<LocalDate> dayOffDates) {
        Trainer trainer = trainerService.getTrainerInfo(trainerId);

        trainerService.checkDayOffDuplicatedOrThrow(trainerId, dayOffDates);
        reservationService.checkConfirmedReservationExistOrThrow(trainerId, dayOffDates);

        List<DayOff> dayOffs = createAndSaveDayOff(trainer, dayOffDates);

        return dayOffs.stream()
                .map(DayOffResult.Response::from)
                .toList();
    }

    private List<DayOff> createAndSaveDayOff(Trainer trainer, List<LocalDate> dayOffDates) {
        List<DayOff> dayOffs = dayOffDates.stream()
                .map(dayOffDate -> DayOff.builder()
                        .trainer(trainer)
                        .dayOffDate(dayOffDate)
                        .build())
                .toList();
        return trainerService.saveAllDayOffs(dayOffs);
    }


    public void deleteDayOff(Long trainerId, Long dayOffId) {
        DayOff dayOff = trainerService.getDayOff(trainerId, dayOffId);
        trainerService.deleteDayOff(dayOff);
    }

    public List<DayOffResult.Response> getDayOff(Long trainerId) {
        List<DayOff> dayOffs = trainerService.findAllDayOff(trainerId);

        return dayOffs.stream()
                .map(DayOffResult.Response::from)
                .toList();
    }

    @Transactional
    public void disconnectTrainer(Long trainerId, Long memberId) {
        PersonalDetail memberDetail = memberService.getMemberDetail(memberId);
        ConnectingInfo connectingInfo = trainerService.getConnectingInfo(trainerId, memberId);
        connectingInfo.disconnect();

        trainerService.saveConnectingInfo(connectingInfo);
        // -> 멤버에게 알림 보내기
        Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
        notificationService.sendNotification(NotificationCommand.Disconnect.of(memberDetail,
                connectingInfo.getTrainer().getTrainerId(), connectingInfo.getTrainer().getName(), UserRole.MEMBER,
                token.getPushToken()));
    }

    @Transactional
    public ConnectRequestDecisionResult decisionConnectRequest(Long trainerId, Long notificationId, Boolean approved) {
        Notification notification = notificationService.getNotification(notificationId);
        if (notification.getNotificationType() != Notification.NotificationType.CONNECT) {
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
        notification.process();
        notificationService.save(notification);

        ConnectingInfo connectingInfo = trainerService.getConnectingInfo(notification.getRefId());
        connectingInfo.decisionConnectRequest(approved);
        trainerService.saveConnectingInfo(connectingInfo);

        PersonalDetail memberDetail = memberService.getMemberDetail(connectingInfo.getMember().getMemberId());
        Token token = authService.getTokenByPersonalDetailId(memberDetail.getPersonalDetailId());
        Trainer trainer = trainerService.getTrainerInfo(trainerId);

        SessionInfo sessionInfo = null;
        if (approved) {
            sessionInfo = trainerService.createSessionInfo(trainer, connectingInfo.getMember());
        }

        notificationService.sendNotification(
                NotificationCommand.ConnectDecision.of(memberDetail, trainer, approved, token.getPushToken())
        );

        return ConnectRequestDecisionResult.of(
                connectingInfo.getMember().getMemberId(),
                sessionInfo != null ? sessionInfo.getSessionInfoId() : null
        );
    }
}
